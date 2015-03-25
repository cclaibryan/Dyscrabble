#!/usr/bin/python

import urllib
import urllib2
import re
import Queue
import time
import os,sys
import inspect
import HTMLParser

#get script path (sys.path and __file__ can not work on jpython)
FILE_PATH = '%s/' % os.path.abspath(os.path.dirname(inspect.stack()[0][1])) 	

#breaking news class
class NewsId :
	def __init__(self, id, type) :
		self.id = id
		self.type = type

	def __eq__(self, other):  
        		if isinstance(other, NewsId):  
            			return ((self.type == other.type) and (self.id == other.id))  
        		else:  
            			return False 
	def __hash__(self):
		return hash(self.type + " " + self.id)

# search for breaking news
def searchBreakingNews(runTimes) :

	#initialization
	baseURL = 'http://www.thestandard.com.hk/'						#main website url of the Standard
	currentTime =  time.strftime('%H:%M:%S %Y-%m-%d',time.localtime(time.time()))	#get current time

	#request for the main page 
	response = myURLOpen(baseURL)
	mainPageContent = response.read()
	
	datePattern = re.findall(r'<!--@today@ \d+  -->', mainPageContent, re.M)
	date = datePattern[0].split(' ')[1]		#get the date like 20150320

	listA = os.listdir(FILE_PATH)
	fileList = []					#file list
	for ele in listA:
		attr = ele.split('--')
		if len(attr)==3 :
			myDict = {}
			myDict['date']=attr[0]
			myDict['id']=attr[1]
			myDict['type']=attr[2].replace('.txt', '')	#need to delete the .txt postfix
			fileList.append(myDict)

	fileList.sort(lambda x, y : cmp(x['date'], y['date']))	#sort before use
	queue = Queue.Queue()
	seen = set()

	urls =  getContentURL(mainPageContent)	#put first group of urls to the queue

	for i in urls:
		queue.put(i)
		seen.add(i)

	times = runTimes-1
	while queue.empty()==False:
		currentUrlId = queue.get()
		currentUrl = '%s%s?id=%s' % (baseURL, currentUrlId.type, currentUrlId.id)
		print "current URL:" + currentUrl

		content = myURLOpen(currentUrl).read()
		reList = getContentURL(content)

		for i in reList:
			if i not in seen:
				queue.put(i)
				seen.add(i)

		pattern = re.compile(r'<span class="bodyHeadline">(.+?)</span>.*?<br/>.*?<span class="bodyCopy">.+?<i>(.+?)</i>(.+?)</span>',re.DOTALL)
		contents = pattern.findall(content)
		

		fileName = FILE_PATH + date + "--" +  currentUrlId.id + "--" + currentUrlId.type + ".txt"
		
		fileInfoDict = {}
		fileInfoDict['id'] = currentUrlId.id
		fileInfoDict['date'] = date
		fileInfoDict['type'] = currentUrlId.type

		#over 300 articles, need to delete some 
		while len(fileList) > 300:
			delFileName = FILE_PATH + fileList[0]['date'] + "--" +  fileList[0]['id'] + "--" + fileList[0]['type'] + ".txt"
			del fileList[0]

			if (os.path.exists(delFileName)):
				os.remove(delFileName)

		htmlParser = HTMLParser.HTMLParser()
		f = open(fileName,'w')
		title = mulReplace(contents[0][0])
		title = htmlParser.unescape(title).encode('gb18030')
		f.write(title)
		f.write('\n')
		myDateString = mulReplace(contents[0][1])
		myDateString = htmlParser.unescape(myDateString).encode('gb18030')
		f.write(myDateString + "  DL at " + currentTime)
		f.write('\n')
		mainCon = mulReplace(contents[0][2])
		mainCon = htmlParser.unescape(mainCon).encode('gb18030')
		f.write(mainCon)
		f.close()

		fileList.append(fileInfoDict)	#add current file info to the list
		if times > 0: times-=1
		else:	break

#url request with try and except
def myURLOpen(url) :
	request = urllib2.Request(url)
	try: response = urllib2.urlopen(request)
	except urllib2.URLError, e:
		if hasattr(e,'code'):
			print e.code
		elif hasattr(e,'reason'):
			print e.reason
	return response

# fix the decode problems
def mulReplace(str):
	str1 = str.replace('&#8216;','\'').replace('&#8217;','\'').replace('&#8220;','\"').replace('&#8221;','\"'). \
	replace('&#8212;','-').replace('&#8211;','-').replace('\'\'','\"').replace('<br/>','').replace('&nbsp;',''). \
	replace('<BR>','').replace('&quot;','"')
	return  re.sub(r"<TABLE(.+?)</TABLE>",'', re.sub('\ +',' ',str1)).strip()

#parse the url and get the value of attributes like type, id
def parseURL(oriUrl):
	oriUrl = oriUrl.replace('?id=',' ').replace('&',' ').replace('href=','').replace('"','')
	strArr = oriUrl.split(' ')
	newId = NewsId(strArr[1], strArr[0])
	return newId

# get the urls from the content
def getContentURL(content):
	pattern = re.compile(r'href="breaking_news_detail\.asp\?id=.+?"')
	x = pattern.findall(content)
	reList = []
	for r in x:
		newId = parseURL(r)
		reList.append(newId)
	return reList

#get the first para to be the run times
searchBreakingNews(int(sys.argv[1]))








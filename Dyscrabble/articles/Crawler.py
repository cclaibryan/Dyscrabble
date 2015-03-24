#!/usr/bin/python

import urllib
import urllib2
import re
import Queue
import time

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
def searchBreakingNews(baseURL,currentTime) :
	response = myURLOpen(baseURL)
	mainPageContent = response.read()
# datePattern = re.findall(r'<!--@today@ \d+  -->', mainPageContent, re.M)
# date = datePattern[0].split(' ')[1]		#get the date like 20150320
# print date
# print mainPageContent
	queue = Queue.Queue()
	seen = set()

	urls =  getContentURL(mainPageContent)	#put first group of urls to the queue

	for i in urls:
		queue.put(i)
		seen.add(i)

	times = 10
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
	
		fileName = currentUrlId.id + "--" + currentUrlId.type + ".txt"
	
		f = open(fileName,'w')
		f.write(mulReplace(contents[0][0]))
		f.write('\n')
		f.write(mulReplace(contents[0][1]) + "  DL at " + currentTime)
		f.write('\n')
		f.write(mulReplace(contents[0][2]))
		f.close()

		if times > 0: times-=1
		else:		break	

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

baseURL = 'http://www.thestandard.com.hk/'						#main website url of the Standard
currentTime =  time.strftime('%H:%M:%S %Y-%m-%d',time.localtime(time.time()))	#get current time
searchBreakingNews(baseURL,currentTime)
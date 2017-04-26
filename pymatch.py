#IMPORTS
from __future__ import division
import numpy as np
import freqfind as freqfind

def match(audio,bpm):
	try:
		out=[]
		inputDict=[]
		sOut=[]
		sizez=len(audio)
		print("len(): "+str(sizez))
		size2=audio.size
		print("size(): "+str(size2))
		ts=1/44100
		duration=(30/bpm)*8*2
		durLen = int(round(duration/ts))
		if bpm == 80:
			durLen=durLen-8
		elif bpm == 110:
			durLen=durLen-4
		if audio.size > durLen:
			audio[0:durLen-1]
		elif audio.size <durLen:
			audio=audio+[0]*(durLen-audio.size-1)
		section=audio
		#section=np.reshape(audio,(round(len(audio)/16),16,))
		valz=int(round(audio.size/16))
		print("Yes?")
		for i in range(0,15):
			tmp=audio[i*valz:valz*(i+1)-1]
			inputDict.append(freqfind.find(tmp,44100))
		print("inputDict: "+str(inputDict))
		return inputDict
	except Exception as e:
		print("[pymatch] ERR: "+str(e))

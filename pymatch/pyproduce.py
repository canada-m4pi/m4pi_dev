from __future__ import division
import scipy.io.wavfile as sp
import numpy as np


def prod(freq,bpm):
	try:
		print("Loading Sounds...")
		fs, ride=sp.read("280Hz_Ride.wav")
		#ride=ride.T[2]
		print(ride)
		fs, hat=sp.read("12000Hz_Hat.wav")
		print(hat)
		#hat=hat.T[0]

		fs, kick=sp.read("kick808.wav")
		print(kick)
		kick=kick.T[0]

		fs, snare=sp.read("snr.wav")
		print(snare)
		snare=snare.T[0]

		ts=1/44100 #Calculate Period
		duration=(30/bpm)*8*2
		durLen=int(round(duration/ts))
		if bpm == 80:
			durLen=durLen-8
		if bpm == 110:
			durLen=durLen-4

		musBox=[0]*durLen
		noteLen=int(round(durLen/16))
		print("Producing Matched Audio")
		ind=0
		lst=np.arange(0,durLen-noteLen,noteLen)
		for j in lst:
			if freq[ind]==0:
				musBox[j:noteLen+j-1]=[0]*noteLen
			elif freq[ind]<500:
				musBox[j:len(kick)+j-1]=kick
			elif freq[ind]<3000:
				musBox[j:len(snare)+j-1]=snare
			else:
				musBox[j:len(hat)+j-1]=hat
			ind=ind+1
		return musBox
	except Exception as e:
		print("[pyproduce] ERR: "+str(e))

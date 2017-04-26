import pyproduce as prod
import pymatch as match
import scipy.io.wavfile as sp
import numpy as np
import os.path

def matchWAV(file,fileout,bpm):
	print("--- Running.")
	print("File: "+file)
	if os.path.exists(file):
		print("File exists.")
	fs,voice=sp.read(file)
	print("Voice: "+str(voice))
	#voice=voice.T[0]
	mVoc=match.match(voice,fs)
	if mVoc is None:
		print("No Match was possible.")
	out=prod.prod(mVoc,bpm)
	music=np.asarray(out)
	#sp.write(fileout,fs,music.astype(np.dtype('i2')))
	sp.write(fileout,44100,music.astype('int16'))

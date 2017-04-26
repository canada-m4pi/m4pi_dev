import pyproduce as prod
import pymatch as match
import scipy.io.wavfile as sp
import numpy as np
def main():
	print("About to test.")
	fs,voice=sp.read("110bpmTest2.wav")
	voice=voice.T[0]
	mVoc=match.match(voice,fs)
	out=prod.prod(mVoc,110)
	music=np.asarray(out)
	sp.write("matched_test2_110bpm.wav",fs,music.astype(np.dtype('i2')))
main()

from scipy.fftpack import fft
import numpy as np

def find(x,fs):
	out=0
	try:
		xfft=fft(x) #Finding FFT of wav
		func=np.fft.fftshift(xfft)
		func=abs(func)
		mx=np.max(func[int(round(len(func)/2)):len(func)])
		loc=np.argmax(func[int(round(len(func)/2)):len(func)])
		sep= 44100/len(func)
		if mx<30:
			out=0
		else:
			out=loc*sep
		return out
	except Exception as e:
		print("[freqfind] ERR: "+str(e))


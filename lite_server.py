import os
import glob
import m4py as m
from flask import Flask, send_from_directory, request, render_template, session,json,jsonify,send_file,redirect
from flask_mail import Message, Mail
from flaskext.mysql import MySQL
from werkzeug import generate_password_hash,check_password_hash
from werkzeug.utils import secure_filename
from passlib.hash import pbkdf2_sha256
from subprocess import Popen

#Establishing Upload parameters
UPLOAD_FOLDER = '/home/st4r/Documents/edp_web/uploads'
ALLOWED_EXTENSIONS = set(['m4a'])

#Establish connection to MySQL...
mysql=MySQL()

#Required from the Flask API
app = Flask(__name__)
app.secret_key='m4pi'

#Setting Upload Folder
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

#Connecting to database.
app.config['MYSQL_DATABASE_USER'] = 'root'
app.config['MYSQL_DATABASE_PASSWORD'] = '34DF65sda'
app.config['MYSQL_DATABASE_DB'] = 'm4pi'
app.config['MYSQL_DATABASE_HOST'] = 'localhost'
mysql.init_app(app)

# This configures the automated E-mail service.
app.config.update(dict(
    DEBUG = True,
    MAIL_SERVER = 'smtp.gmail.com',
    MAIL_PORT = 587,
    MAIL_USE_TLS = True,
    MAIL_USE_SSL = False,
    MAIL_USERNAME = 'm4pi.canada@gmail.com',
    MAIL_PASSWORD = 'uniTorontoRye17',
))

mail=Mail(app)

# Method to check for allowed file extensions.
def allowed_file(filename):
	print(filename.rsplit('.', 1)[1].lower())
	return '.' in filename and \
		filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


#####################################################
# Routing to the settings tabbed for a logged in user
# Will send an error if not logged in.
#####################################################
@app.route("/settings")
def settings():
	if session.get('user'):
		return render_template("settings.html",name=session.get('name'),email=session.get('email'))
	else:
		return redirect('/signin')

@app.route('/uploads/<filename>')
def uploaded_file(filename):
	return send_from_directory(app.config['UPLOAD_FOLDER'],filename)

#####################################################
# Routes to either the landing page, or user home
# depending if you are logged in or not.
# If logged in --> go to user home
# Else --> got to landing page.
#####################################################
@app.route("/")
def hello():
	if session.get('user'):
		return redirect('/home')
	else:
		return render_template("index.html")

#####################################################
# Routes to the user home. Throws an error if you are
# not logged in.
#####################################################
@app.route("/home")
def getUserHome():
	if session.get('user'):
		filesToLoad=[]
		titlePre=''.join(e for e in session.get('email') if e.isalnum())
		print("About to check for files for user: "+str(titlePre))
		for f in glob.glob(os.path.join(app.config['UPLOAD_FOLDER'],"*.wav")): # generator, search immediate subdirectories 
			if "_pre" not in f and titlePre in f:
				filesToLoad.append(f.replace(app.config['UPLOAD_FOLDER']+"/",""))
		print(filesToLoad)
		return render_template("userHome.html",name="\n"+session.get('name'),filesToLoad=filesToLoad)
        else:
		return redirect('/signin')

#####################################################
# '/help' renders HTML template with content on
# 	  for helping users.
#####################################################
@app.route("/help")
def getHelp():
        if session.get('user'):
                return render_template("help.html")
        else:
                return render_template('error.html',error='Are you logged in my friend?')

#####################################################
# Routes to the sign-in page (web users only)
#####################################################
@app.route("/signin")
def login():
	return render_template("signin.html")

#####################################################
# '/signout' signs out the user (from website only)
#####################################################
@app.route("/signout")
def logout():
	session.pop('user',None)
	session.pop('name',None)
	session.pop('email',None)
	return redirect('/')


#####################################################
# '/process_signup' will process the signup from a 
#		    a user registration
#####################################################
@app.route("/process_signup",methods=['POST'])
def process_signup():
	print("Trying to start...")
	try:
		firstname=request.form['firstName'] 	#Gets name from form
		lastname=request.form['lastName']	#Gets name from form
		email=request.form['email']		#Gets email from form
		password = request.form['password']	#Gets password from form
		gender=request.form['gender']		#Gets gender from form
		name=firstname+" "+lastname
		print("Going to Register Name: "+name)
		if name and email and password and gender:
	                conn=mysql.connect()
			conn.autocommit(True)
	                cursor=conn.cursor()
			# Authentication takes place in the next line.
			hash_pass = pbkdf2_sha256.encrypt(password, rounds=100000, salt_size=16)
			# Calling the Stored Procedure to mysql database.
			cursor.callproc('sp_createUser',(email,hash_pass,name,gender))
			data= cursor.fetchall()
			cursor.close()
        	        conn.close()

			if len(data)==0:
				msg=Message("Getting Started with m4pi",sender="m4pi.canada@gmail.com",recipients=[email])
				msg.body="How's it going, " + name+"? Welcome to m4pi!"
				mail.send(msg)
				return redirect('/signin')
			else:
				return render_template('error.html',error='This account already exists...')
		else:
			return json.dumps({'html':'<span>Enter the required fields</span>'})
	except Exception as e:
		return render_template('error.html',error=str(e))
	finally:
		print("Done.")

@app.route("/upload",methods=['POST'])
def upload():
	try:
		print("Getting Title")
		title=request.form['title']
		title=title.strip("\n")
		title=''.join(e for e in title if e.isalnum())
		for c in title:
			print c

		print("Title: "+title)
		print("Getting BPM...")
		bpm=int(request.form['bpm'])
		print("BPM: "+str(bpm))
		print("Getting File...")
		file=request.files['uploadedfile']
		print("Checking File!")
		print("Filename: "+str(file.filename))
        	if file and allowed_file(file.filename):
            		filename = secure_filename(file.filename)
            		file.save(os.path.join(app.config['UPLOAD_FOLDER'], title+".m4a"))
			print("[info] File saved as .m4a")
			print("Converting to .wav")
			ps=Popen(["avconv","-i",os.path.join(app.config['UPLOAD_FOLDER'], title+".m4a"),os.path.join(app.config['UPLOAD_FOLDER'], title+"_pre.wav"),"-y"])
			ps.wait()

			m.matchWAV(os.path.join(app.config['UPLOAD_FOLDER'], title+"_pre.wav"),os.path.join(app.config['UPLOAD_FOLDER'], title+".wav"),bpm)
		return "OKAY"
	except Exception as e:
		print("ERR: "+str(e))
		return "ERR"

@app.route("/signup")
def signup():
	return render_template("signup.html")

@app.route("/auth_user",methods=['POST'])
def authUser():
	try:

		user_email=request.form['email']
		user_password=request.form['password']
		con=mysql.connect()
                con.autocommit(True)
		cursor=con.cursor()
		cursor.callproc('sp_verifyUser', (user_email,))
		data=cursor.fetchall()

		if data[0][0] != "User does not exist.":
			if pbkdf2_sha256.verify(user_password, data[0][2]):
				session['user']=data[0][0]
				session['name']=data[0][3]
				session['email']=data[0][1]
				return redirect('/home')
			else:
				return render_template('error.html',error= 'Wrong Password')
		else:
			return render_template('error.html',error='Wrong Email')
	except Exception as l:
		return render_template('error.html',error=str(l))
	finally:
		print("finally...")
		cursor.close()
		con.close()

@app.route("/proc_mobile_signup",methods=['POST'])
def procMobileSignup():
	newUser=request.get_json(force=True)
	name=newUser['name']
	email=newUser['email']
	gender=newUser['gender']
	password=newUser['password']


	try:
		print("[INFO] About signup user (from mobile)\n")
		conn=mysql.connect()
		conn.autocommit(True)
		cursor=conn.cursor()
		hash_pass = pbkdf2_sha256.encrypt(password, rounds=100000, salt_size=16)
		cursor.callproc('sp_createUser',(email,hash_pass,name,gender))
		data= cursor.fetchall()
		cursor.close()

		if len(data)==0:
			print("[INFO] adding user to database.")
			msg=Message("Getting Started with m4pi",sender="m4pi.canada@gmail.com",recipients=[email])
			msg.body="How's it going, " + name+"? Welcome to m4pi!"
			mail.send(msg)
			return json.dumps({"INFO":"Success"})
		else:
			print("[ALERT] user already added.")
			return json.dumps({"INFO":"User Exists"})
	except Exception as e:
		return "Error."

@app.route("/auth_user_mobile",methods=['POST'])
def authMobileUser():
	
	user=request.get_json(force=True)
	user_email=user['email']
	user_password=user['password']
	print("[INFO] Received Data from app.\n")
        try:
                con=mysql.connect()
                con.autocommit(True)
                cursor=con.cursor()
                cursor.callproc('sp_verifyUser', (user_email,))
                data=cursor.fetchall()
                if data[0][0] != "User does not exist.":
                        if pbkdf2_sha256.verify(user_password, data[0][2]):
                                session['user']=data[0][0]
                                session['name']=data[0][3]
                                session['email']=data[0][1]
                                return json.dumps({"INFO":"Success"})
                        else:
				return json.dumps({"INFO": "Wrong Password"})
                else:

			return json.dumps({"INFO":"Wrong e-mail"})
        except Exception as l:
		print("Err: "+str(l))
        finally:
                print("finally...")
                cursor.close()
                con.close()

@app.errorhandler(404)
def page_not_found(e):
    return render_template('error.html',error="OOPS. This doesnt exist."), 404

#if __name__ == "__main__":
#	app.run('0.0.0.0',port=80,threaded=True)

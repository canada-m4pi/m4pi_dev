from gevent.wsgi import WSGIServer
from lite_server import app

print("Serving...")
http_server = WSGIServer(('', 80), app, log=None, spawn=10)
http_server.serve_forever()


import sys, base64

f = open(sys.argv[1], 'rb')
data = f.read()
f.close()
print(base64.b64encode(data))

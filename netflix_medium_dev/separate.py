import commands

name = 'netflix_medium4.dev'
f = open(name + '.html')
count = 1
for line in f:
	filename = '%s-%0d.html' % (name, count)
	ff = open(filename, 'w')
	ff.write(line)
	ff.close()
	
	count += 1
	
	commands.getoutput('tidy -mi -xml %s' % filename)
	
f.close()

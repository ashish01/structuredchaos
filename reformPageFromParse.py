import random
import sys
 
#text = "(HTML(HEAD(TITLE(WSEQ Pat)(WSEQ Verducci)))(BODY(H1(WSEQ All)(WSEQ movies)(WSEQ by)(WSEQ Pat)(WSEQ Verducci))(TABLE(TR(TD(WSEQ Title))(TD(WSEQ Average)(WSEQ Rating))(TD(WSEQ Release)(WSEQ Date))(TD(WSEQ TV)(WSEQ /)(WSEQ MPAA)(WSEQ Rating))(TD(WSEQ Cast))(TD(WSEQ Directed)(WSEQ By))(TD(WSEQ Duration))(TD(WSEQ Genre)))(TR(TD(WSEQ True)(WSEQ Crime))(TD(WSEQ 3)(WSEQ .)(WSEQ 0))(TD(WSEQ 1995))(TD(WSEQ MPAA)(WSEQ RIGHT_ROUND)(WSEQ R)(WSEQ LEFT_ROUND))(TD(WSEQ Kevin)(WSEQ Dillon)(WSEQ ,)(WSEQ Alicia)(WSEQ Silverstone)(WSEQ ,)(WSEQ Bill)(WSEQ Nunn))(TD(WSEQ Pat)(WSEQ Verducci))(TD(WSEQ 7140))(TD(WSEQ Thrillers)(WSEQ ,)(WSEQ Suspense)(WSEQ ,)(WSEQ Lionsgate)(WSEQ Home)(WSEQ Entertainment))))))"
#text = "(HTML-0(HEAD-0(TITLE-0(@TITLE-0(WSEQ-0 Blue)(WSEQ-0 Submarine))(WSEQ-0 6)))(BODY-0(H1-0(@H1-0(WSEQ-0 Blue)(WSEQ-0 Submarine))(WSEQ-0 6))(TABLE-0(@TABLE-0(@TABLE-1(@TABLE-2(TR-1(TD-3(WSEQ-3 Release)(WSEQ-3 Year))(TD-2(WSEQ-2 1999)))(TR-1(TD-3(WSEQ-3 Play)(WSEQ-3 Duration))(TD-0(@TD-3(WSEQ-2 N)(WSEQ-1 /))(WSEQ-0 A))))(TR-0(TD-2(WSEQ-2 Genres))(TD-1(@TD-3(@TD-2(@TD-2(@TD-2(@TD-2(@TD-3(WSEQ-2 Anime)(WSEQ-1 &))(WSEQ-1 Animation))(WSEQ-1 ,))(WSEQ-1 Anime))(WSEQ-1 Sci))(WSEQ-1 -))(WSEQ-0 Fi))))(TR-0(TD-2(WSEQ-2 Cast))(TD-1(@TD-3(WSEQ-2 N)(WSEQ-1 /))(WSEQ-0 A))))(TR-0(TD-2(WSEQ-2 Director))(TD-1(@TD-3(WSEQ-2 N)(WSEQ-1 /))(WSEQ-0 A))))))"
text = "(HTML-0 (HEAD-0 (TITLE-0 (WSEQ-0 Harlem) (WSEQ-0 Nights))) (BODY-0 (H1-0 (WSEQ-0 Harlem) (WSEQ-0 Nights)) (TABLE-0 (@TABLE-0 (@TABLE-1 (@TABLE-2 (TR-1 (TD-3 (WSEQ-3 Release) (WSEQ-3 Year)) (TD-2 (WSEQ-2 1989))) (TR-1 (TD-3 (WSEQ-3 Play) (WSEQ-3 Duration)) (TD-2 (WSEQ-2 6900)))) (TR-0 (TD-2 (WSEQ-2 Genres)) (TD-1 (@TD-3 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-3 (WSEQ-2 Action) (WSEQ-1 &)) (WSEQ-1 Adventure)) (WSEQ-1 ,)) (WSEQ-1 Action)) (WSEQ-1 Comedies)) (WSEQ-1 ,)) (WSEQ-1 Crime)) (WSEQ-1 Action)) (WSEQ-1 ,)) (WSEQ-1 Period)) (WSEQ-1 Pieces)) (WSEQ-1 ,)) (WSEQ-1 20)) (WSEQ-1 th)) (WSEQ-1 Century)) (WSEQ-1 Period)) (WSEQ-1 Pieces)) (WSEQ-1 ,)) (WSEQ-1 Paramount)) (WSEQ-1 Home)) (WSEQ-0 Entertainment)))) (TR-0 (TD-2 (WSEQ-2 Cast)) (TD-1 (@TD-3 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-2 (@TD-3 (WSEQ-2 Eddie) (WSEQ-1 Murphy)) (WSEQ-1 ,)) (WSEQ-1 Richard)) (WSEQ-1 Pryor)) (WSEQ-1 ,)) (WSEQ-1 Redd)) (WSEQ-1 Foxx)) (WSEQ-1 ,)) (WSEQ-1 Danny)) (WSEQ-1 Aiello)) (WSEQ-1 ,)) (WSEQ-1 Michael)) (WSEQ-1 Lerner)) (WSEQ-1 ,)) (WSEQ-1 Della)) (WSEQ-1 Reese)) (WSEQ-1 ,)) (WSEQ-1 Berlinda)) (WSEQ-1 Tolbert)) (WSEQ-1 ,)) (WSEQ-1 Stan)) (WSEQ-1 Shaw)) (WSEQ-1 ,)) (WSEQ-1 Jasmine)) (WSEQ-1 Guy)) (WSEQ-1 ,)) (WSEQ-1 Vic)) (WSEQ-1 Polizos)) (WSEQ-1 ,)) (WSEQ-1 Lela)) (WSEQ-1 Rochon)) (WSEQ-1 ,)) (WSEQ-1 David)) (WSEQ-1 Marciano)) (WSEQ-1 ,)) (WSEQ-1 Arsenio)) (WSEQ-0 Hall)))) (TR-0 (TD-2 (WSEQ-2 Director)) (TD-1 (WSEQ-1 Eddie) (WSEQ-0 Murphy))))))"

colormap = {}
 
def gethtmlcode(text):
	tokens = text.split('-')
	if tokens[0] == 'WSEQ':
		return 'font'
	else:
		return tokens[0]
 
def getColor(text):
	if not colormap.has_key(text):
		colormap[text] = "#%0X%0X%0X" % (random.randint(0,255),random.randint(0,255),random.randint(0,255))
	code = gethtmlcode(text)
	return '<%s style="background-color:%s;padding:2px;margin:2px">%%s</%s>' % (code,colormap[text],code)
 
ws = []
 
buffer = []
 
for c in text:
	#print ws
	#print c
	#sys.stdin.readline()
	if c == '(':
		if not ''.join(buffer).isspace():
			ws.append(''.join(buffer).strip())
		ws.append('(')
		buffer = []
	elif c == ')':
		if not ''.join(buffer).isspace():
			ws.append(''.join(buffer).strip())
		buffer = []
		lst = []
		while True:
			e = ws.pop()
			if e == '(':
				break
			lst.append(e)
		lst.reverse()
		#print lst
		#s = ''.join(lst)
		#sp = s.find(' ')
		#color = getColor(s[:sp])
		#ws.append(color % s[sp:])
		if lst[0].startswith('@'):
			ws.extend(lst[1:])
		else:
			sp = lst[0].find(' ')
			if (sp == -1):
				color = getColor(lst[0])
				ws.append(color % ''.join(lst[1:]))
			else:
				color = getColor(lst[0][:sp])
				ws.append(color % lst[0][sp+1:] + ''.join(lst[1:]))
	else:
		buffer.append(c)
 
print ''.join(ws)
 

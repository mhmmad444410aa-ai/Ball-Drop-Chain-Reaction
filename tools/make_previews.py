from PIL import Image,ImageDraw,ImageFont,ImageFilter
from pathlib import Path
import math
root=Path(__file__).resolve().parents[1]
a=root/'app/src/main/assets/art'
out=root/'previews';out.mkdir(exist_ok=True)
def f(size,bold=True):
    p='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf' if bold else '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
    return ImageFont.truetype(p,size)
def txt(draw,pos,text,size,fill,anchor='mm'):
    draw.text(pos,text,font=f(size),fill=fill,anchor=anchor,stroke_width=2,stroke_fill=(5,17,37))
def button(im,box,text,c1='#ffcf52'):
    d=ImageDraw.Draw(im,'RGBA');d.rounded_rectangle(box,radius=17,fill=c1,outline='#fff0a5',width=3)
    cx=(box[0]+box[2])//2;cy=(box[1]+box[3])//2
    d.text((cx,cy),text,font=f(20),fill=(54,36,9) if c1=='#ffcf52' else 'white',anchor='mm')
def sprite(im,name,center,size,angle=0):
    source=Image.open(a/(name+'.png')).convert('RGBA');source.thumbnail(size,Image.Resampling.LANCZOS)
    if angle:source=source.rotate(angle,Image.Resampling.BICUBIC,expand=True)
    im.paste(source,(int(center[0]-source.width/2),int(center[1]-source.height/2)),source)
menu=Image.open(a/'menu.jpg').convert('RGBA').resize((1280,720))
d=ImageDraw.Draw(menu,'RGBA');d.rounded_rectangle((330,190,954,558),radius=35,fill=(4,20,50,178),outline=(85,192,251,145),width=2)
txt(d,(640,250),'BALL DROP',49,'#fff6dc');txt(d,(640,306),'CHAIN REACTION',24,'#ffd057')
button(menu,(376,357,624,415),'PLAY');button(menu,(656,357,904,415),'CONTINUE','#2374c2')
button(menu,(374,440,535,491),'WORLDS','#2374c2');button(menu,(551,440,721,491),'SETTINGS','#2374c2');button(menu,(739,440,906,491),'ABOUT','#2374c2')
ImageDraw.Draw(menu).text((640,526),'Physics puzzles  •  Offline play  •  No music',font=f(15,False),fill='#b9e3ff',anchor='mm')
menu.convert('RGB').save(out/'main_menu_concept.jpg',quality=93)
game=Image.open(a/'playfield.jpg').convert('RGBA').resize((1280,720))
d=ImageDraw.Draw(game,'RGBA')
for x,y,w,ang in [(220,250,280,12),(650,375,260,12),(1000,475,260,7)]:sprite(game,'plank_long',(x,y),(w,85),angle=ang)
sprite(game,'ball_red',(155,177),(75,75))
for x in [665,718,772]:sprite(game,'domino',(x,322),(31,84),angle=(x-665)//53*15)
sprite(game,'spring',(510,445),(93,108))
sprite(game,'seesaw',(935,549),(242,122))
sprite(game,'target',(1110,469),(95,151))
d=ImageDraw.Draw(game,'RGBA');d.rounded_rectangle((0,0,1280,61),fill=(4,23,58,226));d.text((20,31),'‹  LEVEL 7: Domino Descent',font=f(22),fill='white',anchor='lm');d.text((1250,31),'08s  |  −  +  RESET',font=f(17),fill='#ffdb71',anchor='rm')
d.rounded_rectangle((0,614,1280,720),fill=(4,24,51,240),outline='#5bb7d9',width=2)
d.text((22,661),'TOOLS',font=f(17),fill='#ffcf52',anchor='lm')
for i,name in enumerate(['plank','domino','spring','seesaw','tube']):
    box=(110+i*95,632,190+i*95,695);d.rounded_rectangle(box,radius=12,fill=(18,74,117,255),outline=(246,198,62,255) if i==0 else (48,133,190,255),width=2)
    sprite(game,name,((box[0]+box[2])//2,(box[1]+box[3])//2),(55,45))
for x,title in [(750,'UNDO'),(870,'ROTATE'),(1002,'HINT'),(1133,'RUN')]:
    button(game,(x,637,x+105,694),title,'#ffcf52' if title=='RUN' else '#2374c2')
game.convert('RGB').save(out/'gameplay_concept.jpg',quality=93)
# Store feature illustration: no unsupported marketing claims or fabricated screenshots.
feature=Image.open(a/'menu.jpg').convert('RGB').resize((1024,576))
feature=feature.crop((0,38,1024,538));dark=Image.new('RGBA',feature.size,(0,10,30,70));feature=Image.alpha_composite(feature.convert('RGBA'),dark)
d=ImageDraw.Draw(feature,'RGBA');d.rounded_rectangle((185,145,850,342),radius=30,fill=(3,18,50,172),outline=(255,212,70,160),width=2)
txt(d,(512,222),'BALL DROP',59,'#fff5db');txt(d,(512,298),'CHAIN REACTION',32,'#ffd059')
feature.convert('RGB').save(out/'feature_graphic_1024x500.jpg',quality=93)
print('Saved:',*[p.name for p in out.iterdir()])

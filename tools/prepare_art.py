from PIL import Image, ImageFilter, ImageDraw
from pathlib import Path
import numpy as np, wave, math, struct
root=Path(__file__).resolve().parents[1]
out=root/'app/src/main/assets/art'; out.mkdir(parents=True,exist_ok=True)
src=Path('/mnt/data')
main=Image.open(src/'a_vibrant_high_detail_2d_game_background_main_m.png').convert('RGB')
main.resize((1600,900),Image.Resampling.LANCZOS).save(out/'menu.jpg',quality=91,optimize=True)
collage=Image.open(src/'a_highly_polished_colorful_2d_game_ui_asset_sheet.png').convert('RGB')
W,H=collage.size
sections={'worlds':(0,0,W//2,H//2),'workshop':(W//2,0,W,H//2),'settings':(0,H//2,W//2,H),'victory':(W//2,H//2,W,H)}
for k,v in sections.items(): collage.crop(v).resize((1280,720),Image.Resampling.LANCZOS).save(out/(k+'.jpg'),quality=90,optimize=True)
# Clean gameplay grid taken from central blue area of main scene, without pre-painted playing objects.
center=main.crop((500,185,1120,690)).resize((1600,900),Image.Resampling.LANCZOS)
arr=np.asarray(center).astype(np.float32)
# Darken middle to keep the physics elements readable, preserve light gradients.
arr=(arr*.80).clip(0,255).astype('uint8')
field=Image.fromarray(arr,'RGB'); dr=ImageDraw.Draw(field,'RGBA')
for x in range(0,1600,40): dr.line([(x,0),(x,900)], fill=(50,150,230,16), width=1)
for y in range(0,900,40): dr.line([(0,y),(1600,y)], fill=(50,150,230,16), width=1)
field.save(out/'playfield.jpg',quality=92,optimize=True)
# Sprite origins are transparent RGBA sheets. Strictly crop each asset independently.
sh1=Image.open(src/'ورقة_عناصر_لعبة_سلسلة_الكرات.png').convert('RGBA')
sh2=Image.open(src/'مجموعة_عناصر_ألغاز_خشبية_ومعدنية_لامعة.png').convert('RGBA')
# Name:(sheet,coords) - wide whitespace trimmed later, with small safe inset.
sprites={
 'ball_red':(sh1,(75,55,280,250)),
 'ball_yellow':(sh1,(335,55,535,245)),
 'ball_green':(sh1,(555,55,760,250)),
 'ball_blue':(sh1,(795,55,1010,255)),
 'ball_purple':(sh1,(1027,50,1249,255)),
 'domino':(sh1,(37,271,173,511)),
 'spring':(sh1,(599,275,811,530)),
 'seesaw':(sh1,(812,298,1234,534)),
 'weight':(sh1,(30,549,337,871)),
 'lever':(sh1,(533,551,896,858)),
 'pipe_elbow':(sh1,(878,534,1245,876)),
 'target':(sh1,(10,871,250,1247)),
 'switch':(sh1,(256,954,505,1193)),
 'gate':(sh1,(506,924,859,1193)),
 'launch_pad':(sh1,(836,967,1058,1162)),
 'spark':(sh1,(1039,960,1220,1111)),
 'plank':(sh2,(20,26,462,152)),
 'plank_long':(sh2,(17,174,796,289)),
 'plank_short':(sh2,(505,27,769,150)),
 'ramp':(sh2,(808,0,1248,320)),
 'platform':(sh2,(15,1030,499,1240)),
 'funnel':(sh2,(13,534,373,765)),
 'tube':(sh2,(852,552,1232,742)),
 'tube_turn':(sh2,(22,754,318,987)),
 'gear':(sh2,(973,761,1182,959)),
 'stopper':(sh2,(1032,414,1235,528)),
}
for k,(sheet,box) in sprites.items():
    im=sheet.crop(box)
    a=np.array(im.getchannel('A'))
    # Remove extremely faint alpha pixels from synthetic transparency; retain anti-aliased edges.
    a=np.where(a<40,0, np.minimum(255, ((a.astype(float)-40)*255/215)).astype('uint8')).astype('uint8')
    im.putalpha(Image.fromarray(a))
    bbox=im.getchannel('A').getbbox()
    if bbox: im=im.crop(bbox)
    im.thumbnail((480,360),Image.Resampling.LANCZOS)
    im.save(out/(k+'.png'),optimize=True)
# App store icon: strict RGBA PNG, alpha, 512px, <=1MiB; RGB quantization then expand to RGBA.
icon=Image.open(src/'a_vibrant_highly_detailed_3d_style_game_app_icon.png').convert('RGBA')
icon=icon.resize((512,512),Image.Resampling.LANCZOS)
# 128-color spatial dither off: expanded to 32-bit RGBA, smaller PNG.
alpha=icon.getchannel('A'); rgb=icon.convert('RGB').quantize(colors=128,dither=Image.Dither.NONE).convert('RGB').convert('RGBA'); rgb.putalpha(alpha)
iconout=root/'app/src/main/res/mipmap-xxxhdpi/ic_launcher.png'; rgb.save(iconout,optimize=True,compress_level=9)
# Generate tiny original SFX (no music).
sfx=root/'app/src/main/assets/sfx'; sfx.mkdir(exist_ok=True)
for name,freq,dur in [('tap',550,.075),('bounce',350,.095),('domino',170,.09),('win',740,.18)]:
 rate=22050; N=int(rate*dur); wave_data=[]
 for i in range(N):
  t=i/rate; env=(1-t/dur)**2; f=freq*(1-.2*t/dur)
  sample=0.22*env*(math.sin(2*math.pi*f*t)+.3*math.sin(2*math.pi*f*2*t))
  wave_data.append(struct.pack('<h',max(-32767,min(32767,int(sample*32767)))))
 with wave.open(str(sfx/(name+'.wav')),'wb') as w:
  w.setnchannels(1); w.setsampwidth(2); w.setframerate(rate); w.writeframes(b''.join(wave_data))
print('Created',len(list(out.iterdir())),'art assets; icon bytes',iconout.stat().st_size)

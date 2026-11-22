#!/usr/bin/env python3
"""Offline structural and media checks. Does NOT compile or run Android code."""
from pathlib import Path
from PIL import Image
import re, wave
root=Path(__file__).resolve().parents[1]
assets=root/'app/src/main/assets/art'
source=root/'app/src/main/java/com/mhmmad/balldropchainreaction'
assert (root/'app/build.gradle.kts').exists()
assert (root/'.github/workflows/android.yml').exists()
levels=(source/'LevelCatalog.kt').read_text()
ids=list(map(int,re.findall(r'\bLevel\((\d+),',levels)))
assert ids==list(range(1,21)),f'Level IDs unexpected: {ids}'
for name in ['menu','worlds','workshop','settings','victory','playfield']:
    p=assets/(name+'.webp');assert p.exists(),name
    with Image.open(p) as im: assert im.width>=800 and im.height>=400
sprites=['plank','plank_long','plank_short','ball_red','ball_yellow','ball_green','ball_blue','ball_purple',
         'domino','spring','seesaw','weight','lever','pipe_elbow','target','switch','gate','launch_pad',
         'spark','ramp','platform','funnel','tube','tube_turn','gear','stopper']
for name in sprites:
    p=assets/(name+'.webp');assert p.exists(),name
    with Image.open(p) as im: assert im.mode=='RGBA' and im.width>15 and im.height>15
icon=root/'app/src/main/res/mipmap-xxxhdpi/ic_launcher.png'
with Image.open(icon) as im: assert im.size==(512,512) and im.mode=='RGBA'
assert icon.stat().st_size<=1024*1024
for name in ('tap','bounce','domino','win'):
    with wave.open(str(root/f'app/src/main/assets/sfx/{name}.wav')) as w:
        assert w.getnchannels()==1 and w.getframerate()>=16000
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
assert 'screenOrientation="landscape"' in manifest
assert 'INTERNET' not in manifest
assert 'com.mhmmad.balldropchainreaction' in (root/'app/build.gradle.kts').read_text()
physics=(source/'PhysicsScene.kt').read_text()
for key in ('world.step(1f/60f,8,3)','RevoluteJointDef','applyLinearImpulse','World(Vec2(0f, -10.4f))'):
    assert key in physics,key
ui=(source/'MainActivity.kt').read_text()+(source/'PhysicsGame.kt').read_text()
for key in ('PLAY','Screen.WORLDS','Screen.SETTINGS','scene.pause()','placed.dropLast(1)','awaitEachGesture'):
    assert key in ui,key
assert not list(root.rglob('*.mp3'))
print('PASS: 20 unique level IDs, 26 RGBA sprites, 6 backgrounds, icon specs, 4 SFX, manifest, physics and UI structural checks.')
print('NOT TESTED: Gradle compilation, on-device gameplay, collision correctness or all level solutions.')

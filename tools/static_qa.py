#!/usr/bin/env python3
from pathlib import Path
from PIL import Image
import re, sys, xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
errors=[]
notes=[]

# XML well-formedness
for p in root.rglob('*.xml'):
    try: ET.parse(p)
    except Exception as e: errors.append(f'Invalid XML: {p.relative_to(root)}: {e}')

# String resource references + ES parity
base_file=root/'app/src/main/res/values/strings.xml'
es_file=root/'app/src/main/res/values-es/strings.xml'
base={e.attrib['name'] for e in ET.parse(base_file).getroot() if e.tag=='string'}
es={e.attrib['name'] for e in ET.parse(es_file).getroot() if e.tag=='string'}
refs=set()
for p in (root/'app/src/main/java').rglob('*.kt'):
    refs.update(re.findall(r'R\.string\.([A-Za-z0-9_]+)', p.read_text(encoding='utf-8')))
for name in sorted(refs-base): errors.append(f'Missing base string: {name}')
for name in sorted(base-es): errors.append(f'Missing Spanish string: {name}')

# Play Store field length checks
checks=[('title_es.txt',30),('title_en.txt',30),('short_description_es.txt',80),('short_description_en.txt',80)]
for name,limit in checks:
    text=(root/'STORE'/name).read_text(encoding='utf-8').strip()
    if len(text)>limit: errors.append(f'{name}: {len(text)} chars > {limit}')

# Core Play graphic dimensions
expected={
    'GRAPHICS/icon/noxspeed_icon_512.png':(512,512),
    'GRAPHICS/feature_graphic/feature_graphic_1024x500.png':(1024,500),
}
for rel,size in expected.items():
    got=Image.open(root/rel).size
    if got!=size: errors.append(f'{rel}: {got} != {size}')
for lang in ('es','en'):
    shots=sorted((root/f'GRAPHICS/screenshots_{lang}').glob('*.png'))
    if len(shots)<8: errors.append(f'screenshots_{lang}: expected >=8, got {len(shots)}')
    for p in shots:
        if Image.open(p).size != (1080,1920): errors.append(f'{p.relative_to(root)} has wrong size')

manifest=(root/'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
if 'ACCESS_BACKGROUND_LOCATION' in manifest: errors.append('Unexpected background location permission')
if 'FOREGROUND_SERVICE_LOCATION' in manifest: errors.append('Unexpected foreground location service permission')

# Production replacements are expected to be pending at this stage.
props=(root/'monetization.properties').read_text(encoding='utf-8')
if 'ca-app-pub-3940256099942544' in props:
    notes.append('AdMob official test IDs intentionally present; replace before production release.')

print(f'Kotlin source files: {len(list((root/"app/src/main/java").rglob("*.kt")))}')
print(f'Strings EN/ES: {len(base)}/{len(es)}')
print('Static QA:', 'PASS' if not errors else 'FAIL')
for n in notes: print('NOTE:',n)
for e in errors: print('ERROR:',e)
sys.exit(1 if errors else 0)

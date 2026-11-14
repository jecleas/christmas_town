"""Tiny chiptune synthesizer: pulse-wave melody, triangle bass, bell 'jingles'. Writes 16-bit mono WAVs."""
import math, random, struct, sys, wave, os
SR = 22050
OUT = sys.argv[1]
NOTE = {'C':0,'C#':1,'D':2,'D#':3,'E':4,'F':5,'F#':6,'G':7,'G#':8,'A':9,'A#':10,'B':11}
def freq(n):
    if n == 'R': return 0
    name, octv = n[:-1], int(n[-1])
    return 440 * 2 ** ((NOTE[name] + 12 * (octv + 1) - 69) / 12)

def env(i, n, a=0.006, r=0.04, sus=0.65):
    t, dur = i / SR, n / SR
    if t < a: return t / a
    level = sus + (1 - sus) * math.exp(-(t - a) * 18)
    if t > dur - r: level *= max(0, (dur - t) / r)
    return level

def pulse(f, n, duty=0.25, vol=0.25):
    if f == 0: return [0.0] * n
    return [vol * env(i, n) * (1 if (i * f / SR) % 1 < duty else -1) for i in range(n)]

def tri(f, n, vol=0.35):
    if f == 0: return [0.0] * n
    return [vol * env(i, n, sus=0.8) * (4 * abs((i * f / SR) % 1 - 0.5) - 1) for i in range(n)]

def bell(n, vol=0.22, base=2600):
    """Sleigh-bell jingle: a few high metallic partials + a little noise, quick decay."""
    out = []
    for i in range(n):
        t = i / SR; d = math.exp(-t * 28)
        s = sum(math.sin(2 * math.pi * base * k * t) for k in (1, 1.47, 2.09))
        out.append(vol * d * (s / 3 * 0.7 + random.uniform(-1, 1) * 0.3))
    return out

def mix(track, sig, start):
    need = start + len(sig)
    if need > len(track): track.extend([0.0] * (need - len(track)))
    for i, v in enumerate(sig): track[start + i] += v

def save(name, data):
    peak = max(1e-6, max(abs(x) for x in data))
    with wave.open(os.path.join(OUT, name), 'w') as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(b''.join(struct.pack('<h', int(max(-1, min(1, x / peak * 0.85)) * 32767)) for x in data))

def song(melody, bass_chords, bpm, bells=True, duty=0.25):
    beat = 60 / bpm; track = []; t = 0.0
    for note, beats in melody:
        n = int(beats * beat * SR)
        mix(track, pulse(freq(note), int(n * 0.92), duty), int(t * SR)); t += beats * beat
    total_beats = sum(b for _, b in melody)
    for bar, chord in enumerate(bass_chords):          # one chord per 4 beats, root-fifth bass
        root, fifth = chord
        for q in range(4):
            start = (bar * 4 + q) * beat
            if start >= total_beats * beat: break
            mix(track, tri(freq(root if q % 2 == 0 else fifth), int(beat * SR * 0.85)), int(start * SR))
            if bells and q % 2 == 1: mix(track, bell(int(beat * SR * 0.5)), int(start * SR))
    return track

# Jingle Bells (chorus, public domain)
jb = [('E5',1),('E5',1),('E5',2), ('E5',1),('E5',1),('E5',2), ('E5',1),('G5',1),('C5',1.5),('D5',.5), ('E5',4),
      ('F5',1),('F5',1),('F5',1.5),('F5',.5), ('F5',1),('E5',1),('E5',1),('E5',.5),('E5',.5), ('E5',1),('D5',1),('D5',1),('E5',1), ('D5',2),('G5',2),
      ('E5',1),('E5',1),('E5',2), ('E5',1),('E5',1),('E5',2), ('E5',1),('G5',1),('C5',1.5),('D5',.5), ('E5',4),
      ('F5',1),('F5',1),('F5',1.5),('F5',.5), ('F5',1),('E5',1),('E5',1),('E5',.5),('E5',.5), ('G5',1),('G5',1),('F5',1),('D5',1), ('C5',4)]
C, F, G = ('C3','G3'), ('F3','C4'), ('G3','D4')
save('music_jingle_bells.wav', song(jb, [C,C,C,C,F,C,G,G, C,C,C,C,F,C,G,C], 150))

# Deck the Halls (public domain) — cozier, slower, no bells
dh = [('G4',1.5),('F4',.5),('E4',1),('D4',1), ('C4',1),('D4',1),('E4',1),('C4',1), ('D4',.5),('E4',.5),('F4',.5),('D4',.5),('E4',1.5),('D4',.5), ('C4',1),('B3',1),('C4',2)] * 2 + \
     [('D4',1.5),('E4',.5),('F4',1),('D4',1), ('E4',1.5),('F4',.5),('G4',1),('D4',1), ('E4',.5),('F#4',.5),('G4',1),('A4',.5),('B4',.5),('C5',1), ('B4',1),('A4',1),('G4',2)] + \
     [('G4',1.5),('F4',.5),('E4',1),('D4',1), ('C4',1),('D4',1),('E4',1),('C4',1), ('A4',.5),('A4',.5),('A4',.5),('A4',.5),('G4',1.5),('F4',.5), ('E4',1),('D4',1),('C4',2)]
dh = [(n[:-1] + str(int(n[-1]) + 1), b) for n, b in dh]   # up an octave for a brighter chip sound
save('music_deck_the_halls.wav', song(dh, [C,G,F,C, C,G,F,C, G,C,G,G, C,G,F,C], 112, bells=False, duty=0.5))

# Sound effects
def seq(notes, dur, duty=0.5, vol=0.3):
    out = []
    for n in notes: out += pulse(freq(n), int(dur * SR), duty, vol)
    return out
save('sfx_tap.wav', pulse(freq('C6'), int(0.05 * SR), 0.5))
save('sfx_coins.wav', seq(['B5', 'E6'], 0.09) + bell(int(0.25 * SR), 0.15, 3200))
save('sfx_level_up.wav', seq(['C5', 'E5', 'G5', 'C6', 'E6', 'G6'], 0.08, 0.25) + pulse(freq('C7'), int(0.35 * SR), 0.25))
save('sfx_cookie_ding.wav', [math.sin(2 * math.pi * 1760 * i / SR) * math.exp(-i / SR * 6) * 0.5 + math.sin(2 * math.pi * 2640 * i / SR) * math.exp(-i / SR * 9) * 0.25 for i in range(int(0.8 * SR))])
save('sfx_sleigh_bells.wav', sum([bell(int(0.12 * SR), 0.25, 2400 + 300 * (k % 3)) for k in range(8)], []))
save('sfx_toy_pop.wav', [0.4 * math.sin(2 * math.pi * (300 + 1500 * i / (0.12 * SR)) * i / SR) * (1 - i / (0.12 * SR)) for i in range(int(0.12 * SR))])
save('sfx_chat_message.wav', seq(['G5'], 0.05, 0.5, 0.25) + seq(['D6'], 0.08, 0.5, 0.25))
save('sfx_oops.wav', seq(['E4', 'C4'], 0.12, 0.5, 0.3))
save('sfx_reindeer_munch.wav', sum([[random.uniform(-1, 1) * 0.35 * math.exp(-(i / SR) * 40) for i in range(int(0.09 * SR))] + [0.0] * int(0.05 * SR) for _ in range(3)], []))
save('sfx_present_drop.wav', [0.35 * ((1 if ((i / SR) * (1200 - 900 * i / (0.3 * SR))) % 1 < 0.5 else -1)) * (1 - i / (0.3 * SR)) for i in range(int(0.3 * SR))] + bell(int(0.3 * SR), 0.2))
print('done')

# ---- More songs (all public domain) ----
def song2(melody, chords, bpm, bar_beats, bells=False, duty=0.25, bell_every=2):
    """Like song(), but any time signature: one chord per bar, bass on the bar's first beat and halfway."""
    beat = 60 / bpm; track = []; t = 0.0
    for note, beats in melody:
        n = int(beats * beat * SR)
        mix(track, pulse(freq(note), int(n * 0.92), duty), int(t * SR)); t += beats * beat
    total = t
    for bar, (root, fifth) in enumerate(chords):
        for k, note in ((0, root), (bar_beats / 2, fifth)):
            start = (bar * bar_beats + k) * beat
            if start >= total: break
            mix(track, tri(freq(note), int(beat * SR * bar_beats / 2 * 0.8)), int(start * SR))
        if bells:
            for q in range(1, int(bar_beats), bell_every):
                start = (bar * bar_beats + q) * beat
                if start < total: mix(track, bell(int(beat * SR * 0.5)), int(start * SR))
    return track

Am, Dm, E7 = ('A2','E3'), ('D3','A3'), ('E3','B3')

# We Wish You a Merry Christmas (3/4)
ww = [('G4',1), ('C5',1),('C5',.5),('D5',.5),('C5',.5),('B4',.5), ('A4',1),('A4',1),('A4',1), ('D5',1),('D5',.5),('E5',.5),('D5',.5),('C5',.5),
      ('B4',1),('G4',1),('G4',1), ('E5',1),('E5',.5),('F5',.5),('E5',.5),('D5',.5), ('C5',1),('A4',1),('G4',.5),('G4',.5), ('A4',1),('D5',1),('B4',1), ('C5',2)]
ww = ww + ww[1:]
save('music_we_wish.wav', song2(ww, [C, C, F, G, G, C, F, G, C] * 2, 160, 3, bells=True, duty=0.25))

# Silent Night (6/8, counted in eighth notes) — slow and calm
sn = [('G4',1.5),('A4',.5),('G4',1),('E4',3), ('G4',1.5),('A4',.5),('G4',1),('E4',3), ('D5',2),('D5',1),('B4',3), ('C5',2),('C5',1),('G4',3),
      ('A4',2),('A4',1),('C5',1.5),('B4',.5),('A4',1), ('G4',1.5),('A4',.5),('G4',1),('E4',3), ('A4',2),('A4',1),('C5',1.5),('B4',.5),('A4',1),
      ('G4',1.5),('A4',.5),('G4',1),('E4',3), ('D5',2),('D5',1),('F5',1.5),('D5',.5),('B4',1), ('C5',3),('E5',3), ('C5',1.5),('G4',.5),('E4',1),('G4',1.5),('F4',.5),('D4',1), ('C4',6)]
save('music_silent_night.wav', song2(sn, [C, C, G, C, F, C, F, C, G, C, G, C], 170, 6, duty=0.5))

# Joy to the World (big finale)
jw = [('C5',2),('B4',1.5),('A4',.5),('G4',3),('F4',1),('E4',2),('D4',2),('C4',3),
      ('G4',1),('A4',3),('A4',1),('B4',3),('B4',1),('C5',3),
      ('C5',1),('C5',1),('B4',1),('A4',1),('G4',1),('G4',1.5),('F4',.5),('E4',1),
      ('C5',1),('C5',1),('B4',1),('A4',1),('G4',1),('G4',1.5),('F4',.5),('E4',1),
      ('E4',1),('E4',1),('E4',1),('E4',1),('E4',.5),('F4',.5),('G4',3),
      ('F4',.5),('E4',.5),('D4',1),('D4',1),('D4',1),('D4',.5),('E4',.5),('F4',3),
      ('E4',.5),('D4',.5),('C4',1),('C5',2),('A4',1),('G4',1.5),('F4',.5),('E4',1),('F4',1),('E4',2),('D4',2),('C4',4)]
jw = [(n[:-1] + str(int(n[-1]) + 1), b) for n, b in jw]
save('music_joy_to_the_world.wav', song2(jw, [C, G, C, G, G, C, C, C, C, G, G, F, C, G, C], 132, 4, bells=True, duty=0.25))

# Sleigh Ride: Jingle Bells, faster, with bells on every beat
save('music_sleigh_ride.wav', song2(jb, [C,C,C,C,F,C,G,G, C,C,C,C,F,C,G,C], 190, 4, bells=True, duty=0.125, bell_every=1))
print('more songs done')

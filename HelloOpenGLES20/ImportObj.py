#!/usr/bin/python

import sys

def getNumbers(a):
    na = list()
    for e in a:
        try:
            f = float(e)
#           na.append(f)
            na.append((e, f))
        except ValueError:
            return
    return na

def getIndices(a):
    iaa = list()
    for e in a:
        ia = list()
        aa = e.split('/')
        for i in range(len(aa)):
            if i == 1 and aa[i] == '':  # permit None for texture
                ia.append(0)
                continue
            try:
                idx = int(aa[i])
                assert idx > 0, 'all indices positive'
                ia.append(idx)
            except ValueError:
                return
        n = len(ia)
        if n == 1:
            iaa.append((ia[0], 0, 0))
        elif n == 2:
            iaa.append((ia[0], ia[1], 0))
        elif n == 3:
            iaa.append(tuple(ia))
        else:   # error
            return
    return iaa

def parse(f):
    g_d = dict()
    g_aa = list()

    n = 0
    g = None
    v_a = list()
    vt_a = list()
    vn_a = list()
    f_aa = list()
    incomplete = False
    while True:
        if not incomplete:
            line = f.readline()
##          print line.strip()
        else:
            # use preview read below
            pass
        if not line:
            break
        line = line.strip()
        a = line.split()
        if len(a) == 0:
            continue
        c = a[0]
        if c == '#':
            print 'startsWith "#": |%s|' % line
            continue
        incomplete = a[-1] == '\\'
##      print incomplete
        if c == 'g':
            assert not incomplete, 'g is not complete'
            assert len(a) == 2, 'g command: without exact one name'
            if g:
                # complete group with f_aa
                assert not g_d.has_key(g), 'already has name: |%s|' % g
                d = dict()
                d['v'] = v_a
                d['vt'] = vt_a
                d['vn'] = vn_a
                d['f'] = f_aa
                g_aa.append((g, d))
            v_a = list()
            vt_a = list()
            vn_a = list()
            f_aa = list()
            g = a[1]
            continue

        if incomplete:
            assert c == 'v' or c == 'vt' or c == 'vn', 'valid commands for incomplete are "v", "vt", "vn"'
            pass
        if c == 'f':
            ia = getIndices(a[1:])
##          print ia
            assert ia and (len(ia) == 3 or len(ia) == 4), 'invalid f command: |%s|' % (line)
            f_aa.append(ia)
            continue

        assert c == 'v' or c == 'vt' or c == 'vn', 'invalid command: "%s"' % c
        naa = list()
        if incomplete:
            naa = getNumbers(a[1:-1])
        else:
            naa = getNumbers(a[1:])
        assert naa, '|%s|: not all numbers' % (line)
        while incomplete:
            line = f.readline()
            if not line:
                print 'FATAL: incomplete file'
                break
            aa = line.split()
##          print line
            incomplete = aa[-1] == '\\'
            if incomplete:
                na = getNumbers(aa[:-1])
            else:
                na = getNumbers(aa)
            assert na, '|%s|: not all numbers' % (line)
            naa += na
        if c == 'v' or c == 'vn':
            assert len(naa) == 3, 'not exact three values: %s' % (naa)
        else:
            assert len(naa) == 2, 'not exact two values: %s' % (naa)
        if c == 'v':
            v_a.append(naa)
        elif c == 'vt':
            vt_a.append(naa)
        else:
            vn_a.append(naa)
    if g:
        assert f_aa, 'no faces for group: |%s|' % (g)
        assert not g_d.has_key(g), 'already has name: |%s|' % g
        d = dict()
        d['v'] = v_a
        d['vt'] = vt_a
        d['vn'] = vn_a
        d['f'] = f_aa
        g_aa.append((g, d))

    return g_aa

def checkIndices(g_aa):
    # check indices
    nva = 0
    nvta = 0
    nvna = 0
    for g, d in g_aa:
        nva += len(d['v'])
        nvta += len(d['vt'])
        nvna += len(d['vn'])
    for g, d in g_aa:
        for f_a in d['f']:
            for f in f_a:
                iv, ivt, ivn = f
                assert iv >= 0 and iv <= nva, 'iv: %d, nva = %d' % (iv, nva)
                assert ivt >= 0 and ivt <= nvta, 'ivt: %d, nvta = %d' % (ivt, nvta)
                assert ivn >= 0 and ivn <= nvna, 'ivn: %d, nvna = %d' % (ivn, nvna)

def rebuildObj(g_aa, useFloat = False):
    # rebuild obj
    idx = 1 if useFloat else 0
    for g, d in g_aa:
        print 'g %s' % (g)
        for v in d['v']:
            print 'v %s %s %s' % (v[0][idx], v[1][idx], v[2][idx])
        for vt in d['vt']:
            print 'vt %s %s' % (vt[0][idx], vt[1][idx])
        for vn in d['vn']:
            print 'vn %s %s %s' % (vn[0][idx], vn[1][idx], vn[2][idx])
        for f_a in d['f']:
            print 'f',
            for f in f_a:
                if f[1] == 0:
                    print '%d//%d' % (f[0], f[2]),
                else:
                    print '%d/%d/%d' % (f[0], f[1], f[2]),
            print

def calculateScaleAndCenter(g_aa):
    Xm, XM = None, None    # X
    Ym, YM = None, None    # Y
    Zm, ZM = None, None    # Z
    X = 0
    Y = 0
    Z = 0
    nv = 0
    for g, d in g_aa:
        nv += len(d['v'])
        for v in d['v']:
            x, y, z = v[0][1], v[1][1], v[2][1]
            X += x
            Y += y
            Z += z
            if Xm is None:
                Xm, XM = x, x
            if x < Xm:
                Xm = x
            elif x > XM:
                XM = x
            if Ym is None:
                Ym, YM = y, y
            if y < Ym:
                Ym = y
            elif y > YM:
                YM = y
            if Zm is None:
                Zm, ZM = z, z
            if z < Zm:
                Zm = z
            elif z > ZM:
                ZM = z
    print 'Bounding Box: (%f, %f), (%f, %f), (%f, %f)' % (Xm, XM, Ym, YM, Zm, ZM)
    lx = XM - Xm
    ly = YM - Ym
    lz = ZM - Zm
    L = lx if lx > ly else ly
    if lz > L:
        L = lz
    print '%f %f %f' % (lx, ly, lz)
    print 'L: %f' % L
    print 'Center of Points: (%f, %f, %f)' % (X / nv, Y / nv, Z / nv)
    print 'Center of Bounding Box: (%f, %f, %f)' % ((Xm + XM) / 2, (Ym + YM) / 2, (Zm + ZM) / 2)
    return L, ((Xm + XM) / 2, (Ym + YM) / 2, (Zm + ZM) / 2)
    return L, (X / nv, Y / nv, Z / nv)

def fitToCube(g_aa, L, (CX, CY, CZ)):
    # move Center to Zero
    # scale bounding box by L
    for g, d in g_aa:
        for i in range(len(d['v'])):
            for k in range(3):
                s, f = d['v'][i][k]
                f = (f - C[k]) / L
                d['v'][i][k] = s, f

if __name__ == '__main__':
    if len(sys.argv) == 1:
        print 'usage: '
        sys.exit()
    try:
        f = open(sys.argv[1])
        g_aa = parse(f)
        f.close()
        checkIndices(g_aa)
#       rebuildObj(g_aa)
        L, C = calculateScaleAndCenter(g_aa)
        fitToCube(g_aa, L, C)
        L, C = calculateScaleAndCenter(g_aa)
#       rebuildObj(g_aa, useFloat = True)
        for g, d in g_aa:
            print 'name: |%s|, # of vertices = %d, # of faces = %d' % (g, len(d['v']), len(d['f']))
        fva = open('va', 'w')
        for g, d in g_aa:
            for v in d['v']:
                fva.write('%f\n%f\n%f\n' % (v[0][1], v[1][1], v[2][1]))
        fva.close()
        # object_3 has the smalles number of vertices
        for i in range(len(g_aa)):
            fia = open('ia%d' % (1 + i), 'w')
            g, d = g_aa[i]
            for f in d['f']:
                fia.write('%d\n%d\n%d\n' % (f[0][0] - 1, f[1][0] - 1, f[2][0] - 1))
            fia.close()
        # test it
    except IOError, e:
        print e

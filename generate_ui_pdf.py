"""
DOPAMINE LOCK – Full UI Documentation PDF Generator
Produces a pixel-perfect, professional design-doc PDF for every screen.
"""

import math

from reportlab.lib import colors
from reportlab.lib.colors import Color, HexColor
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas as pdf_canvas

# ─── COLOUR PALETTE ──────────────────────────────────────────────────────────
BG          = HexColor("#000000")
SURFACE     = HexColor("#0A0A0A")
CARD        = HexColor("#111111")
BORDER      = HexColor("#222222")
SUBTLE      = HexColor("#333333")
DIVIDER     = HexColor("#1A1A1A")
WHITE       = HexColor("#FFFFFF")
GREY        = HexColor("#B3B3B3")
DIM         = HexColor("#666666")
ERROR       = HexColor("#CF6679")
ERROR_BG    = HexColor("#1A0608")   # CF6679 @ ~8% on black

PAGE_W, PAGE_H = A4                 # 595 × 841 pt
MARGIN = 18 * mm
PHONE_W = PAGE_W - 2 * MARGIN      # ~159 mm  ≈  451 pt
PHONE_H = PAGE_H - 50 * mm

# ─── HELPERS ─────────────────────────────────────────────────────────────────
def new_page(c, title, subtitle=""):
    c.showPage()
    c.setFillColor(BG)
    c.rect(0, 0, PAGE_W, PAGE_H, fill=1, stroke=0)

    # Header bar
    c.setFillColor(CARD)
    c.rect(0, PAGE_H - 28*mm, PAGE_W, 28*mm, fill=1, stroke=0)
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.5)
    c.line(0, PAGE_H - 28*mm, PAGE_W, PAGE_H - 28*mm)

    # App label
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 7)
    c.drawString(MARGIN, PAGE_H - 10*mm, "DOPAMINE LOCK  ·  UI DOCUMENTATION")

    # Screen title
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 16)
    c.drawString(MARGIN, PAGE_H - 22*mm, title)

    if subtitle:
        c.setFillColor(GREY)
        c.setFont("Helvetica", 8)
        c.drawRightString(PAGE_W - MARGIN, PAGE_H - 22*mm, subtitle)

    return PAGE_H - 33*mm        # y cursor below header


def rounded_rect(c, x, y, w, h, r=4, fill_color=CARD, stroke_color=BORDER,
                 stroke_width=0.5, fill=1, stroke=1):
    c.setFillColor(fill_color)
    c.setStrokeColor(stroke_color)
    c.setLineWidth(stroke_width)
    c.roundRect(x, y, w, h, r, fill=fill, stroke=stroke)


def label(c, text, x, y, size=7, color=GREY, bold=False, align="left",
          letter_spacing=0):
    font = "Helvetica-Bold" if bold else "Helvetica"
    c.setFont(font, size)
    c.setFillColor(color)
    if letter_spacing and len(text) > 1:
        # manual letter-spacing via charSpace
        c._charSpace = letter_spacing
    if align == "right":
        c.drawRightString(x, y, text)
    elif align == "center":
        c.drawCentredString(x, y, text)
    else:
        c.drawString(x, y, text)
    if letter_spacing:
        c._charSpace = 0


def section_header(c, x, y, w, title):
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 7)
    c._charSpace = 1.5
    c.drawString(x, y, title.upper())
    c._charSpace = 0


def dopamine_button(c, x, y, w, h, text, variant="primary"):
    r = 4
    if variant == "primary":
        rounded_rect(c, x, y, w, h, r, WHITE, WHITE)
        c.setFillColor(HexColor("#000000"))
        c.setFont("Helvetica-Bold", 7)
        c._charSpace = 1.2
        c.drawCentredString(x + w/2, y + h/2 - 2.5, text.upper())
        c._charSpace = 0
    elif variant == "secondary":
        rounded_rect(c, x, y, w, h, r, HexColor("#000000"), BORDER)
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 7)
        c._charSpace = 1.2
        c.drawCentredString(x + w/2, y + h/2 - 2.5, text.upper())
        c._charSpace = 0
    elif variant == "danger":
        rounded_rect(c, x, y, w, h, r, ERROR_BG, ERROR)
        c.setFillColor(ERROR)
        c.setFont("Helvetica-Bold", 7)
        c._charSpace = 1.2
        c.drawCentredString(x + w/2, y + h/2 - 2.5, text.upper())
        c._charSpace = 0


def text_field(c, x, y, w, h, lbl, placeholder="", focused=False):
    border_color = WHITE if focused else BORDER
    rounded_rect(c, x, y, w, h, 3, CARD, border_color, 0.6 if focused else 0.4)
    c.setFillColor(GREY)
    c.setFont("Helvetica", 5.5)
    c.drawString(x + 6, y + h - 7, lbl.upper())
    if placeholder:
        c.setFillColor(SUBTLE)
        c.setFont("Helvetica", 6.5)
        c.drawString(x + 6, y + 5, placeholder)


def stat_card(c, x, y, w, h, value, unit, lbl):
    rounded_rect(c, x, y, w, h, 4, CARD, BORDER, 0.4)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 14)
    c.drawString(x + 6, y + h - 18, value + unit)
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 0.8
    c.drawString(x + 6, y + 6, lbl.upper())
    c._charSpace = 0


def progress_bar(c, x, y, w, h, progress, track=DIVIDER, fill=WHITE):
    c.setFillColor(track)
    c.roundRect(x, y, w, h, h/2, fill=1, stroke=0)
    c.setFillColor(fill)
    c.roundRect(x, y, w * progress, h, h/2, fill=1, stroke=0)


def circle(c, cx, cy, r, fill_color=CARD, stroke_color=BORDER, stroke_w=0.5):
    c.setFillColor(fill_color)
    c.setStrokeColor(stroke_color)
    c.setLineWidth(stroke_w)
    c.circle(cx, cy, r, fill=1, stroke=1)


def tag(c, x, y, text, bg=SURFACE, fg=WHITE, border=BORDER, r=3):
    c.setFont("Helvetica-Bold", 5.5)
    tw = c.stringWidth(text, "Helvetica-Bold", 5.5)
    pad = 5
    bw = tw + pad * 2
    bh = 10
    rounded_rect(c, x, y, bw, bh, r, bg, border, 0.4)
    c.setFillColor(fg)
    c.drawString(x + pad, y + 3, text)
    return bw


def arc_ring(c, cx, cy, r, progress, track_color=BORDER, fill_color=WHITE,
             stroke_w=6):
    # track
    c.setStrokeColor(track_color)
    c.setLineWidth(stroke_w)
    c.arc(cx - r, cy - r, cx + r, cy + r, startAng=0, extent=360)
    # progress arc (approximate with many small lines)
    sweep = 360 * progress
    steps = max(int(sweep / 2), 1)
    c.setStrokeColor(fill_color)
    for i in range(steps):
        angle_start = 90 - (i / steps) * sweep
        angle_end   = 90 - ((i+1) / steps) * sweep
        c.arc(cx - r, cy - r, cx + r, cy + r,
              startAng=angle_start, extent=-(sweep / steps))


def bottom_nav(c, x, y, w, h, active="home"):
    # bar bg
    c.setFillColor(SURFACE)
    c.rect(x, y, w, h, fill=1, stroke=0)
    # top border
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.4)
    c.line(x, y + h, x + w, y + h)

    items = [("Home", "home"), ("Focus", "focus"), ("Tasks", "tasks"),
             ("Stats", "stats"), ("Settings", "settings")]
    item_w = w / len(items)
    for i, (name, key) in enumerate(items):
        cx = x + item_w * i + item_w / 2
        is_active = key == active
        col = WHITE if is_active else DIM
        c.setFillColor(col)
        # icon placeholder dot
        c.circle(cx, y + h - 8, 3, fill=1, stroke=0)
        c.setFont("Helvetica-Bold" if is_active else "Helvetica", 5)
        c.setFillColor(col)
        c.drawCentredString(cx, y + 3, name)


def phone_frame(c, y_top, height):
    """Draw a phone-screen frame and return inner x, y, w, h."""
    x = MARGIN
    y = y_top - height
    rounded_rect(c, x, y, PHONE_W, height, 8, BG, BORDER, 0.6)
    return x, y, PHONE_W, height


def divider_line(c, x, y, w):
    c.setStrokeColor(DIVIDER)
    c.setLineWidth(0.3)
    c.line(x, y, x + w, y)


def color_swatch(c, x, y, size, hex_color, name, hex_text):
    c.setFillColor(HexColor(hex_color))
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.3)
    c.roundRect(x, y, size, size, 3, fill=1, stroke=1)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 5.5)
    c.drawString(x + size + 4, y + size/2 + 1, name)
    c.setFillColor(GREY)
    c.setFont("Helvetica", 5)
    c.drawString(x + size + 4, y + size/2 - 6, hex_text)

# ─── COVER PAGE ──────────────────────────────────────────────────────────────
def cover_page(c):
    c.setFillColor(BG)
    c.rect(0, 0, PAGE_W, PAGE_H, fill=1, stroke=0)

    # Decorative border
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.5)
    c.roundRect(MARGIN/2, MARGIN/2, PAGE_W - MARGIN, PAGE_H - MARGIN, 8,
                fill=0, stroke=1)

    # Corner accents
    for ox, oy in [(MARGIN, PAGE_H - MARGIN),
                   (PAGE_W - MARGIN, PAGE_H - MARGIN),
                   (MARGIN, MARGIN),
                   (PAGE_W - MARGIN, MARGIN)]:
        c.setFillColor(WHITE)
        c.circle(ox, oy, 2, fill=1, stroke=0)

    # Logo mark
    cx, cy = PAGE_W / 2, PAGE_H * 0.62
    circle(c, cx, cy, 28, CARD, BORDER, 0.6)
    circle(c, cx, cy, 20, BG, BORDER, 0.4)
    # lock shackle
    c.setStrokeColor(WHITE)
    c.setLineWidth(2)
    c.arc(cx - 8, cy + 2, cx + 8, cy + 22, startAng=0, extent=180)
    # lock body
    c.setFillColor(WHITE)
    c.roundRect(cx - 9, cy - 8, 18, 14, 2, fill=1, stroke=0)
    c.setFillColor(BG)
    c.circle(cx, cy - 2, 3, fill=1, stroke=0)
    c.setFillColor(BG)
    c.rect(cx - 1.5, cy - 5, 3, 5, fill=1, stroke=0)

    # Title block
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 8)
    c._charSpace = 4
    c.drawCentredString(cx, PAGE_H * 0.52, "DOPAMINE LOCK")
    c._charSpace = 0

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 28)
    c.drawCentredString(cx, PAGE_H * 0.44, "UI Design")
    c.drawCentredString(cx, PAGE_H * 0.38, "Documentation")

    c.setFillColor(GREY)
    c.setFont("Helvetica", 9)
    c.drawCentredString(cx, PAGE_H * 0.33, "Complete Screen Reference  ·  Component System  ·  Colour Palette")

    # Red accent line
    c.setStrokeColor(ERROR)
    c.setLineWidth(1.5)
    line_w = 60
    c.line(cx - line_w/2, PAGE_H * 0.305, cx + line_w/2, PAGE_H * 0.305)

    # Meta info
    meta = [
        ("Team", "Team Dobermans"),
        ("Platform", "Android  ·  Jetpack Compose"),
        ("Theme", "Dark Monochromatic"),
        ("Screens", "12 Screens  ·  22 Components"),
        ("Version", "v1.0.0"),
    ]
    start_y = PAGE_H * 0.22
    for key, val in meta:
        c.setFillColor(DIM)
        c.setFont("Helvetica-Bold", 7)
        c.drawString(MARGIN * 2, start_y, key.upper())
        c.setFillColor(WHITE)
        c.setFont("Helvetica", 8)
        c.drawString(MARGIN * 2 + 50, start_y, val)
        start_y -= 14

    # Footer
    c.setFillColor(DIM)
    c.setFont("Helvetica", 6)
    c.drawCentredString(cx, MARGIN, "CONFIDENTIAL  ·  INTERNAL USE ONLY")


# ─── COLOUR PALETTE PAGE ─────────────────────────────────────────────────────
def color_page(c):
    y = new_page(c, "Colour System", "Monochromatic Dark Palette")
    y -= 6

    swatches = [
        ("#000000", "DopamineBlack",   "#000000  ·  Page backgrounds"),
        ("#0A0A0A", "DopamineSurface", "#0A0A0A  ·  Nav bar, status badges"),
        ("#111111", "DopamineCard",    "#111111  ·  Card backgrounds"),
        ("#222222", "DopamineBorder",  "#222222  ·  Card & field borders"),
        ("#333333", "DopamineSubtle",  "#333333  ·  Subtle accents"),
        ("#1A1A1A", "DopamineDivider", "#1A1A1A  ·  List dividers"),
        ("#FFFFFF", "DopamineWhite",   "#FFFFFF  ·  Primary text, active icons"),
        ("#B3B3B3", "DopamineGrey",    "#B3B3B3  ·  Secondary text, labels"),
        ("#666666", "DopamineDim",     "#666666  ·  Muted / disabled text"),
        ("#CF6679", "DopamineError",   "#CF6679  ·  Danger only  —  Log Out, Abandon"),
    ]

    sw = 24
    row_h = 28
    for i, (hex_c, name, desc) in enumerate(swatches):
        row_y = y - i * row_h
        # swatch box
        c.setFillColor(HexColor(hex_c))
        c.setStrokeColor(BORDER)
        c.setLineWidth(0.4)
        c.roundRect(MARGIN, row_y - sw, sw, sw, 3, fill=1, stroke=1)
        # name
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 8)
        c.drawString(MARGIN + sw + 8, row_y - 10, name)
        # desc
        c.setFillColor(GREY)
        c.setFont("Helvetica", 7)
        c.drawString(MARGIN + sw + 8, row_y - 20, desc)

    # Design rule note
    note_y = y - len(swatches) * row_h - 12
    rounded_rect(c, MARGIN, note_y - 30, PHONE_W, 38, 4, CARD, BORDER, 0.4)
    c.setFillColor(ERROR)
    c.setFont("Helvetica-Bold", 7)
    c._charSpace = 1
    c.drawString(MARGIN + 10, note_y - 8, "DESIGN RULE")
    c._charSpace = 0
    c.setFillColor(GREY)
    c.setFont("Helvetica", 7.5)
    c.drawString(MARGIN + 10, note_y - 20,
        "The only hue in the entire app is DopamineError (#CF6679).")
    c.drawString(MARGIN + 10, note_y - 30,
        "It appears exclusively on destructive actions: Log Out and Abandon Mission.")


# ─── TYPOGRAPHY PAGE ─────────────────────────────────────────────────────────
def typography_page(c):
    y = new_page(c, "Typography System", "SansSerif · All-Caps Convention")
    y -= 4

    styles = [
        ("headlineMedium",  28, "Bold",      "Screen titles"),
        ("headlineSmall",   24, "SemiBold",  "Card titles, stat values"),
        ("titleMedium",     16, "SemiBold",  "Row labels, nav items"),
        ("labelLarge",      14, "Medium",    "Sub-headers — 1.25sp spacing"),
        ("labelSmall",      11, "Medium",    "Badges, section labels  —  ALL CAPS"),
        ("bodyMedium",      14, "Normal",    "Body copy, descriptions"),
        ("bodySmall",       12, "Normal",    "Timestamps, subtitles"),
    ]

    row_h = 38
    for i, (name, size, weight, usage) in enumerate(styles):
        row_y = y - i * row_h
        divider_line(c, MARGIN, row_y - row_h + 2, PHONE_W)
        # specimen text scaled down for PDF
        display_size = min(size * 0.6, 18)
        c.setFillColor(WHITE)
        font = "Helvetica-Bold" if weight in ("Bold", "SemiBold", "Medium") else "Helvetica"
        c.setFont(font, display_size)
        c.drawString(MARGIN, row_y - 14, name)
        # metadata
        c.setFillColor(GREY)
        c.setFont("Helvetica", 6)
        c.drawString(MARGIN + 140, row_y - 10, f"{size}sp  ·  {weight}")
        c.drawString(MARGIN + 140, row_y - 19, usage)

    # Button typography note
    note_y = y - len(styles) * row_h - 10
    rounded_rect(c, MARGIN, note_y - 36, PHONE_W, 44, 4, CARD, BORDER, 0.4)
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c._charSpace = 1
    c.drawString(MARGIN + 8, note_y - 8, "BUTTON OVERRIDE")
    c._charSpace = 0
    c.setFillColor(WHITE)
    c.setFont("Helvetica", 7)
    for li, line in enumerate([
        "All buttons: 13sp · Bold · ALL CAPS · letter-spacing 1.5sp",
        "Height fixed at 52dp  ·  Corner radius 8dp",
        "No icon-only buttons — text is always present",
    ]):
        c.drawString(MARGIN + 8, note_y - 18 - li * 9, line)


# ─── SPLASH + ONBOARDING ─────────────────────────────────────────────────────
def splash_page(c):
    y = new_page(c, "01  Splash Screen", "Entry · ~2.2 second fade-in")
    y -= 4

    # Phone frame
    fh = y - MARGIN - 30
    fx, fy, fw, fhh = phone_frame(c, y, fh)

    # Simulated full-bleed image area
    c.setFillColor(SUBTLE)
    c.roundRect(fx + 1, fy + 1, fw - 2, fhh - 2, 7, fill=1, stroke=0)

    # Overlay gradient suggestion
    c.setFillColor(HexColor("#000000"))
    c.setFillAlpha(0.6)
    c.roundRect(fx + 1, fy + 1, fw - 2, fhh - 2, 7, fill=1, stroke=0)
    c.setFillAlpha(1)

    # Centered logo
    cx = fx + fw / 2
    cy = fy + fhh / 2
    circle(c, cx, cy, 22, CARD, BORDER, 0.5)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 9)
    c.drawCentredString(cx, cy - 3, "DL")

    # Caption
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c._charSpace = 2
    c.drawCentredString(cx, cy - 18, "DOPAMINE LOCK")
    c._charSpace = 0

    # Annotations
    c.setFillColor(GREY)
    c.setFont("Helvetica", 7)
    annotations = [
        (fx + fw + 8, fy + fhh * 0.85, "Full-screen image"),
        (fx + fw + 8, fy + fhh * 0.85 - 10, "ContentScale.Crop"),
        (fx + fw + 8, fy + fhh * 0.5, "Animatable alpha"),
        (fx + fw + 8, fy + fhh * 0.5 - 10, "0f → 1f · 1200ms tween"),
        (fx + fw + 8, fy + fhh * 0.2, "Auto-nav after 1000ms"),
        (fx + fw + 8, fy + fhh * 0.2 - 10, "Total display: ~2.2s"),
    ]
    for ax, ay, txt in annotations:
        c.drawString(ax, ay, txt)

    # Draw annotation lines
    c.setStrokeColor(BORDER)
    c.setLineWidth(0.3)
    for ax, ay, _ in annotations[::2]:
        c.line(fx + fw, fy + ay - fy - 3 + fhh*0.05, ax, ay + 3)


def onboarding_page(c):
    y = new_page(c, "02  Onboarding", "4-Page HorizontalPager · First Launch")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy_inner = fy + fhh - 14

    # Status label
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 2.5
    c.drawCentredString(fx + fw/2, cy_inner - 1, "DOPAMINE LOCK")
    c._charSpace = 0
    cy_inner -= 10

    # Headline
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 11)
    c.drawCentredString(fx + fw/2, cy_inner - 4, "Your Focus")
    c.drawCentredString(fx + fw/2, cy_inner - 14, "Operating System")
    cy_inner -= 26

    # Page card
    card_h = 110
    card_y = cy_inner - card_h
    rounded_rect(c, ix, card_y, iw, card_h, 6, CARD, BORDER, 0.4)

    # Icon circle
    icon_cx = ix + iw / 2
    icon_cy = card_y + card_h - 24
    circle(c, icon_cx, icon_cy, 14, HexColor("#1A1A1A"), BORDER, 0.3)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 9)
    c.drawCentredString(icon_cx, icon_cy - 3, "⊘")

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 9)
    c.drawCentredString(ix + iw/2, card_y + card_h - 46, "Block Distractions")

    c.setFillColor(GREY)
    c.setFont("Helvetica", 6.5)
    c.drawCentredString(ix + iw/2, card_y + card_h - 58,
                        "Lock away apps that steal")
    c.drawCentredString(ix + iw/2, card_y + card_h - 67,
                        "your focus. Stay in control.")
    cy_inner = card_y - 10

    # Pager dots
    dot_y = cy_inner - 4
    dot_cx = ix + iw/2
    dots_total = 4
    dot_sp = 10
    start_x = dot_cx - ((dots_total - 1) * dot_sp + 18) / 2
    # active dot (pill)
    c.setFillColor(WHITE)
    c.roundRect(start_x, dot_y - 3, 18, 6, 3, fill=1, stroke=0)
    for di in range(1, dots_total):
        c.setFillColor(BORDER)
        c.circle(start_x + 18 + di * dot_sp, dot_y, 3, fill=1, stroke=0)
    cy_inner = dot_y - 12

    # Buttons
    dopamine_button(c, ix, cy_inner - 14, iw, 14, "Next", "primary")
    cy_inner -= 20
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c._charSpace = 1.5
    c.drawCentredString(ix + iw/2, cy_inner - 6, "SKIP")
    c._charSpace = 0

    # Pages reference
    ref_x = fx + fw + 10
    pages = [
        ("Page 1", "Block", "⊘  Block Distractions"),
        ("Page 2", "Timer", "⏱  Focus Deeply"),
        ("Page 3", "Chart", "📊  Track Progress"),
        ("Page 4", "Gym",   "💪  Build Discipline"),
    ]
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c.drawString(ref_x, fy + fhh - 10, "PAGES")
    for pi, (pg, icon, desc) in enumerate(pages):
        py = fy + fhh - 22 - pi * 18
        c.setFillColor(CARD)
        c.setStrokeColor(BORDER)
        c.setLineWidth(0.3)
        c.roundRect(ref_x, py - 4, 80, 14, 2, fill=1, stroke=1)
        c.setFillColor(GREY)
        c.setFont("Helvetica", 5.5)
        c.drawString(ref_x + 4, py + 4, pg)
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 6)
        c.drawString(ref_x + 4, py - 2, desc)


# ─── AUTH SCREENS ─────────────────────────────────────────────────────────────
def auth_page(c):
    y = new_page(c, "03  Authentication Screens",
                 "Login · Register · Forgot Password")
    y -= 4

    screen_w = (PHONE_W - 8) / 3
    screens = [
        ("LOGIN", [
            ("Email", "your@email.com"),
            ("Password", "••••••••"),
        ], "Sign In", "Continue with Google", True),
        ("REGISTER", [
            ("Full Name",         "John Doe"),
            ("Email",             "your@email.com"),
            ("Password",          "••••••••"),
            ("Confirm Password",  "••••••••"),
        ], "Create Account", "Cancel", False),
        ("FORGOT PW", [
            ("Email Address", "your@email.com"),
        ], "Send Reset Link", None, False),
    ]

    for si, (title, fields, btn1, btn2, show_google) in enumerate(screens):
        sx = MARGIN + si * (screen_w + 4)
        fh_s = y - MARGIN - 20
        sy = y - fh_s

        rounded_rect(c, sx, sy, screen_w, fh_s, 5, BG, BORDER, 0.5)
        iy = sy + fh_s - 10

        # Screen title label
        c.setFillColor(GREY)
        c.setFont("Helvetica-Bold", 5)
        c._charSpace = 1.5
        c.drawCentredString(sx + screen_w/2, iy - 1, "DOPAMINE LOCK")
        c._charSpace = 0
        iy -= 9

        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 9)
        headline = {"LOGIN": "Welcome Back",
                    "REGISTER": "Create Account",
                    "FORGOT PW": "Reset Password"}[title]
        c.drawCentredString(sx + screen_w/2, iy - 2, headline)
        iy -= 10

        c.setFillColor(GREY)
        c.setFont("Helvetica", 5.5)
        sub = {"LOGIN":     "Sign in to continue",
               "REGISTER":  "Start your journey",
               "FORGOT PW": "We'll send a reset link"}[title]
        c.drawCentredString(sx + screen_w/2, iy, sub)
        iy -= 12

        pad = 5
        fw_inner = screen_w - pad * 2
        for fi, (lbl, ph) in enumerate(fields):
            text_field(c, sx + pad, iy - 18, fw_inner, 18, lbl, ph)
            iy -= 22

        if title == "LOGIN":
            c.setFillColor(WHITE)
            c.setFont("Helvetica", 5.5)
            c.drawRightString(sx + screen_w - pad, iy + 2, "Forgot Password?")
            iy -= 6

        iy -= 4
        dopamine_button(c, sx + pad, iy - 14, fw_inner, 14, btn1, "primary")
        iy -= 18

        if show_google:
            # OR divider
            c.setStrokeColor(BORDER)
            c.setLineWidth(0.3)
            c.line(sx + pad, iy - 2, sx + screen_w - pad, iy - 2)
            c.setFillColor(GREY)
            c.setFont("Helvetica", 5)
            c.drawCentredString(sx + screen_w/2, iy - 1.5, "OR")
            iy -= 10
            dopamine_button(c, sx + pad, iy - 14, fw_inner, 14,
                            "Continue with Google", "secondary")
            iy -= 18

        if btn2:
            dopamine_button(c, sx + pad, iy - 14, fw_inner, 14, btn2,
                            "secondary")


# ─── DASHBOARD ───────────────────────────────────────────────────────────────
def dashboard_page(c):
    y = new_page(c, "04  Dashboard", "Home Tab · Main Overview")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    # Header
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 1.5
    c.drawString(ix, cy, "GOOD MORNING")
    c._charSpace = 0
    cy -= 9
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 12)
    c.drawString(ix, cy, "Aayush")
    # notification + avatar
    circle(c, ix + iw - 14, cy + 4, 7, CARD, BORDER, 0.4)
    circle(c, ix + iw - 28, cy + 4, 7, CARD, BORDER, 0.4)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 6)
    c.drawCentredString(ix + iw - 28, cy + 1, "A")
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    c.drawCentredString(ix + iw - 14, cy + 1, "🔔")
    cy -= 14

    # Stats row
    section_header(c, ix, cy, iw, "Today's Stats")
    cy -= 8
    sw = (iw - 8) / 3
    for si, (val, unit, lbl) in enumerate([("4.2", "h", "Focus Hrs"),
                                            ("6", "", "Sessions"),
                                            ("12", "", "Day Streak")]):
        stat_card(c, ix + si * (sw + 4), cy - 28, sw, 28, val, unit, lbl)
    cy -= 36

    # Goal progress
    section_header(c, ix, cy, iw, "Today's Goal")
    cy -= 8
    rounded_rect(c, ix, cy - 32, iw, 32, 4, CARD, BORDER, 0.4)
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1
    c.drawString(ix + 8, cy - 10, "DAILY FOCUS GOAL")
    c._charSpace = 0
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 7)
    c.drawRightString(ix + iw - 8, cy - 10, "70%")
    progress_bar(c, ix + 8, cy - 20, iw - 16, 4, 0.70)
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    c.drawString(ix + 8, cy - 30, "4 / 6 hours")
    cy -= 40

    # Quick actions
    section_header(c, ix, cy, iw, "Quick Actions")
    cy -= 8
    hw = (iw - 6) / 2
    dopamine_button(c, ix, cy - 14, hw, 14, "▶ Start Focus", "primary")
    dopamine_button(c, ix + hw + 6, cy - 14, hw, 14, "⏱ My Tasks", "secondary")
    cy -= 22

    # Recent sessions
    section_header(c, ix, cy, iw, "Recent Sessions")
    cy -= 2
    sessions = [
        ("Deep Work — Code Review", "52min", True),
        ("Reading Session",         "30min", True),
        ("Design Sprint",           "45min", True),
        ("Morning Focus Block",     "60min", False),
        ("Documentation",           "25min", True),
    ]
    for sname, dur, done in sessions:
        if cy - 12 < fy + 20:
            break
        cy -= 12
        divider_line(c, ix, cy + 12, iw)
        col = WHITE if done else GREY
        c.setFillColor(col)
        c.setFont("Helvetica", 5.5)
        icon = "✓" if done else "○"
        c.drawString(ix, cy + 2, icon + "  " + sname)
        tag(c, ix + iw - 26, cy - 1, dur, SURFACE, WHITE, BORDER)
    cy -= 4

    # Bottom nav
    bottom_nav(c, fx, fy, fw, 22, "home")


# ─── FOCUS TIMER ─────────────────────────────────────────────────────────────
def focus_timer_page(c):
    y = new_page(c, "05  Focus Timer", "Pomodoro · Canvas Arc · 3 States")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    # Top bar
    circle(c, ix + 8, cy, 8, CARD, BORDER, 0.4)
    c.setFillColor(WHITE)
    c.setFont("Helvetica", 7)
    c.drawCentredString(ix + 8, cy - 2.5, "←")
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1.5
    c.drawCentredString(fx + fw/2, cy, "FOCUS SESSION")
    c._charSpace = 0
    cy -= 14

    # Status badge
    rounded_rect(c, fx + fw/2 - 22, cy - 10, 44, 10, 3, SURFACE, SURFACE, 0)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1.5
    c.drawCentredString(fx + fw/2, cy - 4, "IN PROGRESS")
    c._charSpace = 0
    cy -= 18

    # Circular timer
    ring_r = 34
    ring_cx = fx + fw / 2
    ring_cy = cy - ring_r - 4
    # track
    c.setStrokeColor(BORDER)
    c.setLineWidth(4)
    c.circle(ring_cx, ring_cy, ring_r, fill=0, stroke=1)
    # progress arc (~65%)
    progress_deg = 360 * 0.65
    steps = 80
    c.setStrokeColor(WHITE)
    c.setLineWidth(4)
    for step in range(steps):
        a1 = math.radians(90 - (step / steps) * progress_deg)
        a2 = math.radians(90 - ((step + 1) / steps) * progress_deg)
        x1 = ring_cx + ring_r * math.cos(a1)
        y1 = ring_cy + ring_r * math.sin(a1)
        x2 = ring_cx + ring_r * math.cos(a2)
        y2 = ring_cy + ring_r * math.sin(a2)
        c.setLineWidth(4)
        p = c.beginPath()
        p.moveTo(x1, y1)
        p.lineTo(x2, y2)
        c.drawPath(p, fill=0, stroke=1)

    # Time text
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 18)
    c.drawCentredString(ring_cx, ring_cy - 4, "16:15")
    c.setFillColor(GREY)
    c.setFont("Helvetica", 5.5)
    c._charSpace = 1.5
    c.drawCentredString(ring_cx, ring_cy - 13, "running")
    c._charSpace = 0
    cy = ring_cy - ring_r - 10

    # Label
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c._charSpace = 2
    c.drawCentredString(fx + fw/2, cy, "DEEP WORK")
    c._charSpace = 0
    c.setFillColor(DIM)
    c.setFont("Helvetica", 5.5)
    c.drawCentredString(fx + fw/2, cy - 8, "25-minute focus block")
    cy -= 20

    # Button row (Running state)
    hw = (iw - 6) / 2
    dopamine_button(c, ix, cy - 14, hw, 14, "⏸ Pause",  "secondary")
    dopamine_button(c, ix + hw + 6, cy - 14, hw, 14, "⏹ End", "danger")
    cy -= 20
    dopamine_button(c, ix, cy - 14, iw, 14, "Enter Mission Mode", "secondary")

    # State annotations
    ref_x = fx + fw + 8
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c.drawString(ref_x, fy + fhh - 10, "TIMER STATES")
    states = [
        ("Idle",    "[ START SESSION ]", "primary"),
        ("Running", "[ PAUSE ] [ END ]", "both"),
        ("Paused",  "[ RESUME ] [ END ]","both"),
    ]
    for si2, (st, btns, _) in enumerate(states):
        sy2 = fy + fhh - 25 - si2 * 28
        rounded_rect(c, ref_x, sy2 - 18, 82, 24, 3, CARD, BORDER, 0.3)
        c.setFillColor(WHITE)
        c.setFont("Helvetica-Bold", 6)
        c.drawString(ref_x + 4, sy2 - 6, st)
        c.setFillColor(GREY)
        c.setFont("Helvetica", 5.5)
        c.drawString(ref_x + 4, sy2 - 15, btns)


# ─── MISSION MODE ────────────────────────────────────────────────────────────
def mission_page(c):
    y = new_page(c, "06  Mission Mode", "Maximum Focus · No Bottom Nav")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 16

    # Lock icon circle
    lock_cx = fx + fw / 2
    lock_cy = cy - 16
    circle(c, lock_cx, lock_cy, 16, BG, BORDER, 0.6)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 12)
    c.drawCentredString(lock_cx, lock_cy - 4, "🔒")
    cy = lock_cy - 20

    # MISSION ACTIVE badge
    badge_w = 60
    rounded_rect(c, lock_cx - badge_w/2, cy - 8, badge_w, 8, 2, WHITE, WHITE)
    c.setFillColor(HexColor("#000000"))
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 1
    c.drawCentredString(lock_cx, cy - 3.5, "MISSION ACTIVE")
    c._charSpace = 0
    cy -= 14

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 10)
    c.drawCentredString(lock_cx, cy, "MISSION MODE")
    cy -= 8
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    c.drawCentredString(lock_cx, cy, "Maximum focus. Zero distractions.")
    cy -= 14

    # Elapsed card
    rounded_rect(c, ix, cy - 26, iw, 26, 4, CARD, BORDER, 0.4)
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1.5
    c.drawCentredString(lock_cx, cy - 8, "TIME ELAPSED")
    c._charSpace = 0
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 14)
    c.drawCentredString(lock_cx, cy - 22, "00:04:32")
    cy -= 32

    # Mission rules
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1.5
    c.drawString(ix, cy, "MISSION RULES")
    c._charSpace = 0
    cy -= 6
    rules = [
        "📱  No social media or distracting apps",
        "🔕  Notifications are silenced",
        "🚫  Blocked apps cannot be accessed",
        "📶  Internet restricted to work tools only",
        "🔒  Exiting mission mode is permanent",
    ]
    for rule in rules:
        if cy - 13 < fy + 52:
            break
        rounded_rect(c, ix, cy - 12, iw, 12, 3, SURFACE, SURFACE, 0)
        c.setFillColor(GREY)
        c.setFont("Helvetica", 6)
        c.drawString(ix + 6, cy - 8, rule)
        c.setFillColor(WHITE)
        c.drawRightString(ix + iw - 6, cy - 8, "✓")
        cy -= 15

    # Warning box
    if cy - 24 > fy + 36:
        c.setFillColor(ERROR_BG)
        c.setStrokeColor(ERROR)
        c.setLineWidth(0.4)
        c.roundRect(ix, cy - 24, iw, 24, 3, fill=1, stroke=1)
        c.setFillColor(ERROR)
        c.setFont("Helvetica-Bold", 5.5)
        c._charSpace = 1
        c.drawString(ix + 6, cy - 8, "WARNING")
        c._charSpace = 0
        c.setFont("Helvetica", 5.5)
        c.drawString(ix + 6, cy - 16,
                     "Abandoning resets your streak. Cannot be undone.")
        cy -= 30

    if cy - 14 > fy + 16:
        dopamine_button(c, ix, cy - 14, iw, 14, "Abandon Mission", "danger")


# ─── TASKS ───────────────────────────────────────────────────────────────────
def tasks_page(c):
    y = new_page(c, "07  Tasks Screen", "Filter Chips · Priority Badges · FAB")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    # Header
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 2
    c.drawString(ix, cy, "MY TASKS")
    c._charSpace = 0
    tag(c, ix + iw - 32, cy - 4, "5 pending", CARD, WHITE, BORDER)
    cy -= 9
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 12)
    c.drawString(ix, cy, "8 items")
    cy -= 14

    # Filter chips
    filters = [("ALL", True), ("PENDING", False), ("COMPLETED", False)]
    cx_chip = ix
    for fname, active in filters:
        fw_chip = len(fname) * 4.5 + 12
        bg = WHITE if active else HexColor("#000000")
        fg = HexColor("#000000") if active else GREY
        border = WHITE if active else BORDER
        rounded_rect(c, cx_chip, cy - 10, fw_chip, 10, 3, bg, border, 0.4)
        c.setFillColor(fg)
        c.setFont("Helvetica-Bold" if active else "Helvetica", 5.5)
        c.drawCentredString(cx_chip + fw_chip/2, cy - 6, fname)
        cx_chip += fw_chip + 5
    cy -= 18

    # Tasks
    task_data = [
        ("Design system audit",        "Design",      "Today",  "HIGH",   False),
        ("Write quarterly report",      "Work",        "Tmrw",   "HIGH",   False),
        ("Code review for PR #47",      "Development", "Today",  "MEDIUM", True),
        ("Update dependencies",         "Development", "Jun 25", "LOW",    False),
        ("Morning meditation routine",  "Health",      "Daily",  "MEDIUM", True),
    ]
    for tname, tcat, tdue, tprio, tdone in task_data:
        if cy - 22 < fy + 30:
            break
        rounded_rect(c, ix, cy - 20, iw, 20, 4, CARD, BORDER, 0.4)
        col = GREY if tdone else WHITE
        c.setFillColor(col)
        c.setFont("Helvetica", 5.5)
        c.drawString(ix + 6, cy - 8, ("✓  " if tdone else "○  ") + tname)
        # priority badge
        pcol = WHITE if tprio == "HIGH" else GREY
        c.setFillColor(pcol)
        c.setFont("Helvetica-Bold", 4.5)
        c.drawRightString(ix + iw - 6, cy - 8, tprio)
        # chips
        tag(c, ix + 14, cy - 17, tcat, SURFACE, GREY, BORDER)
        tag(c, ix + 14 + len(tcat) * 4 + 14, cy - 17, tdue, SURFACE, GREY, BORDER)
        cy -= 23

    # FAB
    circle(c, fx + fw - 18, fy + 28, 10, WHITE, WHITE)
    c.setFillColor(HexColor("#000000"))
    c.setFont("Helvetica-Bold", 10)
    c.drawCentredString(fx + fw - 18, fy + 24.5, "+")

    bottom_nav(c, fx, fy, fw, 22, "tasks")


# ─── ADD/EDIT TASK ───────────────────────────────────────────────────────────
def add_task_page(c):
    y = new_page(c, "08  Add / Edit Task", "Category Grid · Priority Selector")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    # Back btn + title
    circle(c, ix + 8, cy, 8, CARD, BORDER, 0.4)
    c.setFillColor(WHITE)
    c.setFont("Helvetica", 7)
    c.drawCentredString(ix + 8, cy - 2.5, "←")
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1.5
    c.drawCentredString(fx + fw/2, cy, "NEW TASK")
    c._charSpace = 0
    cy -= 16

    # Fields
    text_field(c, ix, cy - 18, iw, 18, "Task Title", "What needs to be done?")
    cy -= 24
    text_field(c, ix, cy - 24, iw, 24, "Description",
               "Add details or notes...", False)
    cy -= 30

    # Category
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1
    c.drawString(ix, cy, "CATEGORY")
    c._charSpace = 0
    cy -= 8
    cats = ["Work", "Dev", "Design", "Health", "Learn", "Personal"]
    cw = (iw - 8) / 3
    for ci, cat in enumerate(cats):
        row = ci // 3
        col = ci % 3
        cx2 = ix + col * (cw + 4)
        cy2 = cy - row * 14 - 12
        selected = cat == "Design"
        bg = WHITE if selected else CARD
        fg = HexColor("#000000") if selected else GREY
        rounded_rect(c, cx2, cy2, cw, 12, 3, bg, WHITE if selected else BORDER, 0.4)
        c.setFillColor(fg)
        c.setFont("Helvetica-Bold" if selected else "Helvetica", 5)
        c.drawCentredString(cx2 + cw/2, cy2 + 4, cat)
    cy -= 34

    # Due date
    text_field(c, ix, cy - 18, iw, 18, "Due Date", "e.g. Jun 25, 2025")
    cy -= 26

    # Priority
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1
    c.drawString(ix, cy, "PRIORITY")
    c._charSpace = 0
    cy -= 8
    prios = ["High", "Medium", "Low"]
    pw = (iw - 8) / 3
    for pi, prio in enumerate(prios):
        px = ix + pi * (pw + 4)
        sel = prio == "Medium"
        rounded_rect(c, px, cy - 12, pw, 12, 3,
                     WHITE if sel else CARD,
                     WHITE if sel else BORDER, 0.4)
        c.setFillColor(HexColor("#000000") if sel else GREY)
        c.setFont("Helvetica-Bold" if sel else "Helvetica", 5)
        c.drawCentredString(px + pw/2, cy - 8, prio)
    cy -= 20

    dopamine_button(c, ix, cy - 14, iw, 14, "Create Task",  "primary")
    cy -= 20
    dopamine_button(c, ix, cy - 14, iw, 14, "Cancel", "secondary")


# ─── ANALYTICS ───────────────────────────────────────────────────────────────
def analytics_page(c):
    y = new_page(c, "09  Analytics", "Canvas Charts · No Third-Party Lib")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 2
    c.drawString(ix, cy, "ANALYTICS")
    c._charSpace = 0
    cy -= 8
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 11)
    c.drawString(ix, cy, "This Week")
    cy -= 14

    # Stat cards
    sw = (iw - 8) / 3
    for si, (val, unit, lbl) in enumerate([("29.2", "h", "Total Hrs"),
                                            ("38",   "", "Sessions"),
                                            ("12",   "", "Day Streak")]):
        stat_card(c, ix + si * (sw + 4), cy - 28, sw, 28, val, unit, lbl)
    cy -= 36

    # Bar chart
    section_header(c, ix, cy, iw, "Weekly Focus Hours")
    cy -= 8
    chart_h = 45
    rounded_rect(c, ix, cy - chart_h, iw, chart_h, 4, CARD, BORDER, 0.4)
    weekly = [("Mon", 2.5), ("Tue", 4.0), ("Wed", 3.2),
              ("Thu", 5.5), ("Fri", 6.0), ("Sat", 3.8), ("Sun", 4.2)]
    max_v = 6.0
    bar_w_each = (iw - 10) / len(weekly)
    for di, (day, val) in enumerate(weekly):
        bx = ix + 5 + di * bar_w_each
        bh_px = (val / max_v) * (chart_h - 18)
        is_today = day == "Fri"
        bar_fill = WHITE if is_today else HexColor("#4D4D4D")
        c.setFillColor(bar_fill)
        c.roundRect(bx + 1, cy - chart_h + 10, bar_w_each - 4, bh_px, 1,
                    fill=1, stroke=0)
        c.setFillColor(WHITE if is_today else GREY)
        c.setFont("Helvetica-Bold" if is_today else "Helvetica", 4.5)
        c.drawCentredString(bx + bar_w_each/2, cy - chart_h + 4, day)
        c.setFillColor(GREY)
        c.setFont("Helvetica", 4)
        c.drawCentredString(bx + bar_w_each/2, cy - chart_h + chart_h - 8,
                            f"{val}h")
    cy -= chart_h + 8

    # Line chart
    section_header(c, ix, cy, iw, "Monthly Trend")
    cy -= 8
    line_h = 35
    rounded_rect(c, ix, cy - line_h, iw, line_h, 4, CARD, BORDER, 0.4)
    monthly = [3.1,2.8,4.5,5.2,4.8,6.0,5.5,4.2,3.9,5.8,
               6.2,4.5,3.7,5.0,5.5,6.3,5.8,4.1,3.5,4.9,
               5.7,6.0,5.3,4.8,5.2,5.9,6.1,4.7,5.0,4.3]
    max_m = max(monthly)
    pad_x = 6
    chartw = iw - pad_x * 2
    charth = line_h - 8
    pts = []
    for mi, mv in enumerate(monthly):
        px2 = ix + pad_x + mi * chartw / (len(monthly) - 1)
        py2 = cy - line_h + 4 + charth * (mv / max_m)
        pts.append((px2, py2))
    # fill
    path = c.beginPath()
    path.moveTo(pts[0][0], cy - line_h + 4)
    for px2, py2 in pts:
        path.lineTo(px2, py2)
    path.lineTo(pts[-1][0], cy - line_h + 4)
    path.close()
    c.setFillColor(HexColor("#FFFFFF"))
    c.setFillAlpha(0.08)
    c.drawPath(path, fill=1, stroke=0)
    c.setFillAlpha(1)
    # line
    c.setStrokeColor(WHITE)
    c.setLineWidth(1)
    for i in range(len(pts) - 1):
        c.line(pts[i][0], pts[i][1], pts[i+1][0], pts[i+1][1])
    cy -= line_h + 8

    # Distribution
    section_header(c, ix, cy, iw, "Focus Distribution")
    cy -= 6
    cats2 = [("Deep Work", 24, 45), ("Learning", 14, 26),
             ("Design", 8, 15),    ("Meetings", 7, 14)]
    for cat, hrs, pct in cats2:
        if cy - 18 < fy + 28:
            break
        rounded_rect(c, ix, cy - 18, iw, 16, 3, SURFACE, BORDER, 0.3)
        c.setFillColor(WHITE)
        c.setFont("Helvetica", 6)
        c.drawString(ix + 6, cy - 8, cat)
        c.setFont("Helvetica-Bold", 6)
        c.drawRightString(ix + iw - 6, cy - 8, f"{pct}%")
        c.setFillColor(GREY)
        c.setFont("Helvetica", 5)
        c.drawRightString(ix + iw - 20, cy - 8, f"{hrs}h")
        progress_bar(c, ix + 6, cy - 16, iw - 12, 2, pct / 100)
        cy -= 21

    bottom_nav(c, fx, fy, fw, 22, "stats")


# ─── SETTINGS ────────────────────────────────────────────────────────────────
def settings_page(c):
    y = new_page(c, "10  Settings", "Profile · Toggles · Nav Rows")
    y -= 4
    fh = y - MARGIN - 16
    fx, fy, fw, fhh = phone_frame(c, y, fh)
    ix = fx + 8
    iw = fw - 16
    cy = fy + fhh - 14

    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 2
    c.drawString(ix, cy, "SETTINGS")
    c._charSpace = 0
    cy -= 8
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 11)
    c.drawString(ix, cy, "Preferences")
    cy -= 14

    # Profile card
    rounded_rect(c, ix, cy - 30, iw, 30, 4, CARD, BORDER, 0.4)
    circle(c, ix + 18, cy - 15, 10, SURFACE, BORDER, 0.5)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 7)
    c.drawCentredString(ix + 18, cy - 18, "A")
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 7)
    c.drawString(ix + 32, cy - 9, "Aayush")
    c.setFillColor(GREY)
    c.setFont("Helvetica", 5.5)
    c.drawString(ix + 32, cy - 17, "aayushdada01@gmail.com")
    tag(c, ix + 32, cy - 27, "12-DAY STREAK", SURFACE, WHITE, BORDER)
    c.setFillColor(GREY)
    c.setFont("Helvetica", 7)
    c.drawRightString(ix + iw - 6, cy - 14, "›")
    cy -= 36

    # Account group
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 1.5
    c.drawString(ix, cy, "ACCOUNT")
    c._charSpace = 0
    cy -= 6
    rows_acct = [("👤", "Edit Profile"), ("🔑", "Change Password"),
                 ("🔒", "Privacy & Security")]
    group_h = len(rows_acct) * 14 + 2
    rounded_rect(c, ix, cy - group_h, iw, group_h, 4, CARD, BORDER, 0.4)
    for ri, (icon, lbl) in enumerate(rows_acct):
        ry = cy - ri * 14 - 10
        c.setFillColor(GREY)
        c.setFont("Helvetica", 6)
        c.drawString(ix + 6, ry, icon + "  " + lbl)
        c.drawRightString(ix + iw - 6, ry, "›")
        if ri < len(rows_acct) - 1:
            divider_line(c, ix + 20, ry - 4, iw - 20)
    cy -= group_h + 8

    # Focus group
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5)
    c._charSpace = 1.5
    c.drawString(ix, cy, "FOCUS")
    c._charSpace = 0
    cy -= 6
    focus_rows = [
        ("⏱", "Strict Focus Mode",    None,     False),
        ("⏱", "Default Session",      "25 min", None),
        ("⏱", "Break Duration",       "5 min",  None),
    ]
    group_h2 = len(focus_rows) * 14 + 2
    rounded_rect(c, ix, cy - group_h2, iw, group_h2, 4, CARD, BORDER, 0.4)
    for ri, (icon, lbl, trailing, toggled) in enumerate(focus_rows):
        ry = cy - ri * 14 - 10
        c.setFillColor(GREY)
        c.setFont("Helvetica", 6)
        c.drawString(ix + 6, ry, icon + "  " + lbl)
        if trailing:
            c.setFillColor(GREY)
            c.setFont("Helvetica", 5.5)
            c.drawRightString(ix + iw - 14, ry, trailing)
            c.drawRightString(ix + iw - 6, ry, "›")
        else:
            # draw tiny toggle
            tog_x = ix + iw - 22
            tog_y = ry - 4
            c.setFillColor(SURFACE)
            c.setStrokeColor(BORDER)
            c.setLineWidth(0.3)
            c.roundRect(tog_x, tog_y, 14, 7, 3.5, fill=1, stroke=1)
            c.setFillColor(GREY)
            c.circle(tog_x + 4, tog_y + 3.5, 2.5, fill=1, stroke=0)
        if ri < len(focus_rows) - 1:
            divider_line(c, ix + 20, ry - 4, iw - 20)
    cy -= group_h2 + 10

    # Logout button
    if cy - 14 > fy + 24:
        dopamine_button(c, ix, cy - 14, iw, 14, "Log Out", "danger")
        cy -= 20

    # Version
    if cy - 10 > fy + 20:
        c.setFillColor(DIM)
        c.setFont("Helvetica", 5)
        c._charSpace = 0.8
        c.drawCentredString(fx + fw/2, cy - 6, "DOPAMINE LOCK v1.0.0")
        c._charSpace = 0

    bottom_nav(c, fx, fy, fw, 22, "settings")


# ─── COMPONENT SYSTEM PAGE ───────────────────────────────────────────────────
def components_page(c):
    y = new_page(c, "11  Component System", "Buttons · Cards · Fields · Navigation")
    y -= 6

    col_w = (PHONE_W - 8) / 2
    lx = MARGIN           # left column x
    rx = MARGIN + col_w + 8  # right column x

    # ── LEFT COLUMN ──
    # Buttons
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(lx, y, "DopamineButton")
    y2 = y - 10
    dopamine_button(c, lx, y2 - 14, col_w, 14, "Primary Action", "primary")
    y2 -= 20
    dopamine_button(c, lx, y2 - 14, col_w, 14, "Secondary Action", "secondary")
    y2 -= 20
    dopamine_button(c, lx, y2 - 14, col_w, 14, "Danger Action", "danger")
    y2 -= 20

    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    specs_btn = [
        "Height: 52dp · Corner: 8dp",
        "Font: 13sp · Bold · ALL CAPS",
        "Letter spacing: 1.5sp",
        "Primary: White fill, Black text",
        "Secondary: Transparent, White border",
        "Danger: #CF6679@15% fill, #CF6679 text",
    ]
    for spec in specs_btn:
        c.drawString(lx, y2, spec)
        y2 -= 7
    y2 -= 8

    # Text field
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(lx, y2, "DopamineTextField")
    y2 -= 8
    text_field(c, lx, y2 - 18, col_w, 18, "Email", "your@email.com", False)
    y2 -= 24
    text_field(c, lx, y2 - 18, col_w, 18, "Password", "••••••••", True)
    y2 -= 26

    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    specs_tf = [
        "Background: #111111 (DopamineCard)",
        "Unfocused border: #222222",
        "Focused border:   #FFFFFF",
        "Label: ALL CAPS · grey unfocused, white focused",
        "Cursor: White",
    ]
    for spec in specs_tf:
        c.drawString(lx, y2, spec)
        y2 -= 7

    # ── RIGHT COLUMN ──
    ry = y - 0

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(rx, ry, "DashboardStatCard")
    ry -= 8
    stat_card(c, rx, ry - 30, col_w, 30, "4.2", "h", "Focus Hrs")
    ry -= 38

    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    for spec in ["Background: #111111  ·  Border: #222222",
                 "Value: 28sp Bold White",
                 "Label: 5.5sp · ALL CAPS · Grey · 1.2sp spacing"]:
        c.drawString(rx, ry, spec)
        ry -= 7
    ry -= 6

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(rx, ry, "FocusProgressCard")
    ry -= 8
    rounded_rect(c, rx, ry - 32, col_w, 32, 4, CARD, BORDER, 0.4)
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 5.5)
    c._charSpace = 1
    c.drawString(rx + 6, ry - 8, "DAILY FOCUS GOAL")
    c._charSpace = 0
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 7)
    c.drawRightString(rx + col_w - 6, ry - 8, "70%")
    progress_bar(c, rx + 6, ry - 18, col_w - 12, 4, 0.70)
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    c.drawString(rx + 6, ry - 28, "4 / 6 hours")
    ry -= 40

    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    for spec in ["Track: #1A1A1A  ·  Fill: White",
                 "Height: 4dp  ·  Clipped rounded corners"]:
        c.drawString(rx, ry, spec)
        ry -= 7
    ry -= 6

    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(rx, ry, "Bottom Navigation Bar")
    ry -= 8
    bottom_nav(c, rx, ry - 22, col_w, 22, "focus")
    ry -= 30
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    for spec in ["Height: 64dp  ·  Background: #0A0A0A",
                 "Top border: 1dp #222222",
                 "Active: filled icon + bold white label",
                 "Inactive: outlined icon + grey label",
                 "Indicator pill: Transparent (removed)"]:
        c.drawString(rx, ry, spec)
        ry -= 7

    ry -= 6
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 8)
    c.drawString(rx, ry, "DopamineCard")
    ry -= 8
    rounded_rect(c, rx, ry - 28, col_w, 28, 4, CARD, BORDER, 0.4)
    c.setFillColor(WHITE)
    c.setFont("Helvetica-Bold", 7)
    c.drawString(rx + 8, ry - 12, "Card Title")
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    c.drawString(rx + 8, ry - 22, "Background #111111 · Border #222222")
    ry -= 36
    c.setFillColor(GREY)
    c.setFont("Helvetica", 6)
    for spec in ["Corner radius: 12dp",
                 "Elevation: 0dp (flat — no shadow)",
                 "Padding: 16dp"]:
        c.drawString(rx, ry, spec)
        ry -= 7


# ─── NAVIGATION FLOW PAGE ────────────────────────────────────────────────────
def nav_flow_page(c):
    y = new_page(c, "12  Navigation Architecture",
                 "Sealed Routes · Stack Management")
    y -= 6

    node_w, node_h, node_r = 68, 18, 4
    col_gap = 14
    row_gap = 28

    def node(nx, ny, text, bg=CARD, fg=WHITE, border=BORDER):
        rounded_rect(c, nx, ny - node_h, node_w, node_h, node_r, bg, border, 0.5)
        c.setFillColor(fg)
        c.setFont("Helvetica-Bold", 6)
        c.drawCentredString(nx + node_w/2, ny - node_h/2 - 2, text)

    def arrow(x1, y1, x2, y2):
        c.setStrokeColor(BORDER)
        c.setLineWidth(0.4)
        c.line(x1, y1, x2, y2)
        # arrowhead
        ang = math.atan2(y2 - y1, x2 - x1)
        asize = 4
        c.line(x2, y2,
               x2 - asize * math.cos(ang - 0.4),
               y2 - asize * math.sin(ang - 0.4))
        c.line(x2, y2,
               x2 - asize * math.cos(ang + 0.4),
               y2 - asize * math.sin(ang + 0.4))

    start_x = MARGIN
    start_y = y

    # Auth flow (left column)
    node(start_x, start_y, "SPLASH", CARD, WHITE, WHITE)
    arrow(start_x + node_w/2, start_y - node_h,
          start_x + node_w/2, start_y - node_h - row_gap + node_h)
    node(start_x, start_y - row_gap, "ONBOARDING", CARD, WHITE, WHITE)
    arrow(start_x + node_w/2, start_y - row_gap - node_h,
          start_x + node_w/2, start_y - row_gap*2 - node_h + node_h)
    node(start_x, start_y - row_gap*2, "LOGIN", CARD, WHITE, WHITE)

    # From Login: branches
    login_cx = start_x + node_w/2
    login_by = start_y - row_gap*2 - node_h

    # Register branch
    reg_x = start_x + node_w + col_gap
    node(reg_x, start_y - row_gap*3, "REGISTER", CARD, GREY, BORDER)
    arrow(login_cx, login_by,
          reg_x + node_w/2, start_y - row_gap*3)

    # Forgot PW branch
    fp_x = start_x
    node(fp_x, start_y - row_gap*3, "FORGOT PW", CARD, GREY, BORDER)
    arrow(login_cx, login_by,
          fp_x + node_w/2, start_y - row_gap*3)

    # Both go to Dashboard
    dash_x = start_x + node_w + col_gap
    dash_y = start_y - row_gap*2

    node(dash_x, dash_y, "DASHBOARD", WHITE, HexColor("#000000"), WHITE)

    arrow(login_cx, login_by,
          dash_x + node_w/2, dash_y)
    arrow(reg_x + node_w/2, start_y - row_gap*3 - node_h,
          dash_x + node_w/2, dash_y)

    # From Dashboard: bottom nav tabs
    tabs = ["FOCUS", "TASKS", "ANALYTICS", "SETTINGS"]
    tab_y = dash_y - row_gap * 2
    total_tab_w = len(tabs) * (node_w + col_gap) - col_gap
    tab_start_x = MARGIN

    for ti, tab in enumerate(tabs):
        tx = tab_start_x + ti * (node_w + col_gap)
        node(tx, tab_y, tab, CARD, WHITE, BORDER)
        arrow(dash_x + node_w/2, dash_y - node_h,
              tx + node_w/2, tab_y)

    # Focus → Mission Mode
    focus_x = tab_start_x
    mission_x = focus_x
    mission_y = tab_y - row_gap
    node(mission_x, mission_y, "MISSION MODE", ERROR_BG, ERROR, ERROR)
    arrow(focus_x + node_w/2, tab_y - node_h,
          mission_x + node_w/2, mission_y)

    # Tasks → Add Task
    tasks_x = tab_start_x + (node_w + col_gap)
    add_task_x = tasks_x
    add_task_y = tab_y - row_gap
    node(add_task_x, add_task_y, "ADD TASK", CARD, GREY, BORDER)
    arrow(tasks_x + node_w/2, tab_y - node_h,
          add_task_x + node_w/2, add_task_y)

    # Logout from settings → Login
    set_x = tab_start_x + 3 * (node_w + col_gap)
    logout_label_x = set_x + node_w + 4
    c.setFillColor(ERROR)
    c.setFont("Helvetica", 5.5)
    c.drawString(logout_label_x, tab_y - node_h/2, "LOGOUT →")
    c.drawString(logout_label_x, tab_y - node_h/2 - 7, "clears backstack")

    # Legend
    leg_y = mission_y - row_gap * 1.5
    c.setFillColor(GREY)
    c.setFont("Helvetica-Bold", 6)
    c.drawString(MARGIN, leg_y, "ROUTE REFERENCE")
    routes = [
        ("splash", "SplashScreen"),
        ("onboarding", "OnboardingScreen"),
        ("login", "LoginScreen"),
        ("register", "RegisterScreen"),
        ("forgot_password", "ForgotPasswordScreen"),
        ("dashboard", "DashboardScreen"),
        ("focus", "FocusTimerScreen"),
        ("mission", "MissionModeScreen"),
        ("tasks", "TasksScreen"),
        ("add_task", "AddEditTaskScreen"),
        ("analytics", "AnalyticsScreen"),
        ("settings", "SettingsScreen"),
    ]
    col1_x = MARGIN
    col2_x = MARGIN + 120
    for ri, (route, screen) in enumerate(routes):
        row_y = leg_y - 10 - ri % 6 * 10
        col_x = col1_x if ri < 6 else col2_x
        c.setFillColor(DIM)
        c.setFont("Helvetica", 5.5)
        c.drawString(col_x, row_y, f'"{route}"')
        c.setFillColor(WHITE)
        c.drawString(col_x + 60, row_y, f"→  {screen}")


# ─── MAIN ────────────────────────────────────────────────────────────────────
def main():
    out_path = "DOPAMINE_LOCK_UI_Documentation.pdf"
    c = pdf_canvas.Canvas(out_path, pagesize=A4)
    c.setTitle("Dopamine Lock – UI Documentation")
    c.setAuthor("Team Dobermans")
    c.setSubject("Android App UI Design Reference")

    cover_page(c)
    color_page(c)
    typography_page(c)
    splash_page(c)
    onboarding_page(c)
    auth_page(c)
    dashboard_page(c)
    focus_timer_page(c)
    mission_page(c)
    tasks_page(c)
    add_task_page(c)
    analytics_page(c)
    settings_page(c)
    components_page(c)
    nav_flow_page(c)

    c.save()
    print(f"✅  PDF saved → {out_path}")
    print(f"    Pages: 15  |  Screens: 12  |  Components: full system")


if __name__ == "__main__":
    main()

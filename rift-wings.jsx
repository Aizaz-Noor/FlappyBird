
import { useState, useEffect, useRef, useCallback } from "react";

const W = 420, H = 680;
const BIRD_X = 90;
const BIRD_R = 14;
const PIPE_W = 52;
const GAP = 155;
const GRAV = 0.42;
const FLAP = -8.2;
const BASE_SPEED = 2.6;
const SPAWN_INTERVAL = 95;

const DIMS = [
  { name: "ALPHA", color: "#00f5ff", dark: "#002a2e", glow: "rgba(0,245,255,0.25)", bg: "#00f5ff11" },
  { name: "BETA", color: "#39ff14", dark: "#0a2200", glow: "rgba(57,255,20,0.25)", bg: "#39ff1411" },
  { name: "GAMMA", color: "#ff2d78", dark: "#2e000f", glow: "rgba(255,45,120,0.25)", bg: "#ff2d7811" },
];

function rng(min, max) { return Math.random() * (max - min) + min; }
function newGapY() { return rng(GAP / 2 + 50, H - GAP / 2 - 50); }

export default function RiftWings() {
  const cvs = useRef(null);
  const G = useRef(null);
  const raf = useRef(null);
  const keys = useRef({});
  const shiftPressed = useRef(false);

  const [phase, setPhase] = useState("menu"); // menu | playing | dead
  const [score, setScore] = useState(0);
  const [best, setBest] = useState(0);
  const [dim, setDim] = useState(0);
  const [hi, setHi] = useState({ score: 0, dim: 0 });

  // ── Init ────────────────────────────────────────────────────────────────────
  const init = useCallback(() => {
    G.current = {
      bird: { y: H / 2, vy: -3, dim: 0, shiftCD: 0, dead: false, angle: 0 },
      pipes: [[], [], []],
      timer: 0,
      score: 0,
      speed: BASE_SPEED,
      particles: [],
      ripples: [],
      trail: [],
      stars: Array.from({ length: 90 }, () => ({
        x: rng(0, W), y: rng(0, H),
        sz: rng(0.4, 2.2), spd: rng(0.15, 0.6), op: rng(0.2, 0.9),
      })),
      flash: 0, flashColor: "#fff",
      combo: 0, comboTimer: 0,
    };
    setScore(0);
    setDim(0);
  }, []);

  // ── Controls ────────────────────────────────────────────────────────────────
  const flap = useCallback(() => {
    const g = G.current;
    if (!g || g.bird.dead) return;
    g.bird.vy = FLAP;
    g.bird.angle = -0.45;
    for (let i = 0; i < 6; i++) {
      g.particles.push({
        x: BIRD_X - 8, y: g.bird.y + rng(-8, 8),
        vx: rng(-2.5, -0.5), vy: rng(-1.5, 1.5),
        life: 18, maxLife: 18,
        color: DIMS[g.bird.dim].color, sz: rng(2, 4),
      });
    }
  }, []);

  const shiftDim = useCallback((dir) => {
    const g = G.current;
    if (!g || g.bird.dead || g.bird.shiftCD > 0) return;
    const prev = g.bird.dim;
    g.bird.dim = (g.bird.dim + dir + 3) % 3;
    g.bird.shiftCD = 22;
    g.flash = 12;
    g.flashColor = DIMS[g.bird.dim].color;
    // Ripple
    g.ripples.push({ x: BIRD_X, y: g.bird.y, r: 0, maxR: 80, color: DIMS[prev].color, life: 20 });
    // Burst
    for (let i = 0; i < 18; i++) {
      const a = (i / 18) * Math.PI * 2;
      g.particles.push({
        x: BIRD_X, y: g.bird.y,
        vx: Math.cos(a) * rng(2, 5), vy: Math.sin(a) * rng(2, 5),
        life: 22, maxLife: 22,
        color: DIMS[prev].color, sz: rng(2, 5),
      });
    }
    setDim(g.bird.dim);
  }, []);

  const handleInput = useCallback((e) => {
    const k = e.key;
    if (phase === "playing") {
      if (k === " " || k === "ArrowUp" || k === "w" || k === "W") { e.preventDefault(); flap(); }
      if ((k === "ArrowLeft" || k === "a" || k === "A" || k === "q" || k === "Q") && !shiftPressed.current) { e.preventDefault(); shiftPressed.current = true; shiftDim(-1); }
      if ((k === "ArrowRight" || k === "d" || k === "D" || k === "e" || k === "E") && !shiftPressed.current) { e.preventDefault(); shiftPressed.current = true; shiftDim(1); }
    } else {
      if (k === " " || k === "Enter") { e.preventDefault(); init(); setPhase("playing"); }
    }
  }, [phase, flap, shiftDim, init]);

  const handleKeyUp = useCallback((e) => { shiftPressed.current = false; }, []);

  useEffect(() => {
    window.addEventListener("keydown", handleInput);
    window.addEventListener("keyup", handleKeyUp);
    return () => { window.removeEventListener("keydown", handleInput); window.removeEventListener("keyup", handleKeyUp); };
  }, [handleInput, handleKeyUp]);

  // ── Tap ─────────────────────────────────────────────────────────────────────
  const tap = useCallback(() => {
    if (phase === "playing") { flap(); }
    else { init(); setPhase("playing"); }
  }, [phase, flap, init]);

  // ── Game Loop ────────────────────────────────────────────────────────────────
  useEffect(() => {
    const canvas = cvs.current;
    const ctx = canvas.getContext("2d");

    // ── Drawing helpers ───────────────────────────────────────────────────────
    function drawPipe(x, gapY, d, alpha) {
      const { color, dark } = DIMS[d];
      ctx.globalAlpha = alpha;

      // Top pipe body
      const g1 = ctx.createLinearGradient(x, 0, x + PIPE_W, 0);
      g1.addColorStop(0, dark); g1.addColorStop(0.4, color + "55"); g1.addColorStop(1, dark);
      ctx.shadowColor = color; ctx.shadowBlur = alpha > 0.5 ? 14 : 3;
      ctx.fillStyle = g1;
      ctx.fillRect(x, 0, PIPE_W, gapY - GAP / 2);

      // Top cap
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.roundRect(x - 5, gapY - GAP / 2 - 14, PIPE_W + 10, 14, [3, 3, 0, 0]);
      ctx.fill();

      // Bottom pipe body
      const g2 = ctx.createLinearGradient(x, 0, x + PIPE_W, 0);
      g2.addColorStop(0, dark); g2.addColorStop(0.4, color + "55"); g2.addColorStop(1, dark);
      ctx.fillStyle = g2;
      ctx.fillRect(x, gapY + GAP / 2, PIPE_W, H - (gapY + GAP / 2));

      // Bottom cap
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.roundRect(x - 5, gapY + GAP / 2, PIPE_W + 10, 14, [0, 0, 3, 3]);
      ctx.fill();

      // Gap highlight line
      ctx.strokeStyle = color + "44"; ctx.lineWidth = 1;
      ctx.beginPath(); ctx.moveTo(x - 5, gapY); ctx.lineTo(x + PIPE_W + 5, gapY); ctx.stroke();

      ctx.shadowBlur = 0; ctx.globalAlpha = 1;
    }

    function drawBird(x, y, d, angle) {
      const { color } = DIMS[d];
      ctx.save();
      ctx.translate(x, y);
      ctx.rotate(angle);
      ctx.shadowColor = color; ctx.shadowBlur = 22;

      // Body
      ctx.fillStyle = color;
      ctx.beginPath();
      ctx.ellipse(0, 0, BIRD_R, BIRD_R * 0.78, 0, 0, Math.PI * 2);
      ctx.fill();

      // Inner glow
      const ig = ctx.createRadialGradient(0, 0, 0, 0, 0, BIRD_R);
      ig.addColorStop(0, "#ffffff99"); ig.addColorStop(1, "transparent");
      ctx.fillStyle = ig;
      ctx.beginPath();
      ctx.ellipse(0, 0, BIRD_R, BIRD_R * 0.78, 0, 0, Math.PI * 2);
      ctx.fill();

      // Wing
      ctx.fillStyle = color + "cc";
      ctx.beginPath();
      ctx.ellipse(-4, 3, 9, 4, -0.4, 0, Math.PI * 2);
      ctx.fill();

      // Eye
      ctx.shadowBlur = 0;
      ctx.fillStyle = "#0a0a0a";
      ctx.beginPath(); ctx.arc(7, -4, 5.5, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = "#fff";
      ctx.beginPath(); ctx.arc(7, -4, 4, 0, Math.PI * 2); ctx.fill();
      ctx.fillStyle = "#111";
      ctx.beginPath(); ctx.arc(8.5, -4, 2.5, 0, Math.PI * 2); ctx.fill();
      // Eye shine
      ctx.fillStyle = "#fff";
      ctx.beginPath(); ctx.arc(9.5, -5.5, 1, 0, Math.PI * 2); ctx.fill();

      // Beak
      ctx.fillStyle = "#ffcc44";
      ctx.beginPath(); ctx.moveTo(13, -2); ctx.lineTo(20, 0); ctx.lineTo(13, 2); ctx.closePath(); ctx.fill();

      ctx.restore();
    }

    function loop() {
      const g = G.current;

      // BG
      ctx.fillStyle = "#020408";
      ctx.fillRect(0, 0, W, H);

      // Stars
      if (g) {
        g.stars.forEach(s => {
          ctx.globalAlpha = s.op;
          ctx.fillStyle = "#e0f0ff";
          ctx.beginPath(); ctx.arc(s.x, s.y, s.sz, 0, Math.PI * 2); ctx.fill();
          if (phase === "playing") {
            s.x -= s.spd * g.speed * 0.4;
            if (s.x < 0) { s.x = W; s.y = rng(0, H); }
          }
        });
        ctx.globalAlpha = 1;
      }

      // Scanlines
      for (let y = 0; y < H; y += 4) {
        ctx.fillStyle = "rgba(0,0,0,0.08)";
        ctx.fillRect(0, y, W, 2);
      }

      // ── MENU / DEAD ─────────────────────────────────────────────────────────
      if (phase !== "playing" || !g) {
        if (phase === "menu") {
          // Dim badges
          DIMS.forEach((d, i) => {
            ctx.fillStyle = d.color + "18";
            ctx.fillRect(0, 0, W, H / 3);
          });

          ctx.textAlign = "center";
          ctx.fillStyle = "#00f5ff";
          ctx.shadowColor = "#00f5ff"; ctx.shadowBlur = 40;
          ctx.font = "bold 44px 'Courier New'";
          ctx.fillText("RIFT", W / 2, H / 2 - 90);
          ctx.fillStyle = "#ff2d78";
          ctx.shadowColor = "#ff2d78";
          ctx.fillText("WINGS", W / 2, H / 2 - 42);
          ctx.shadowBlur = 0;
          ctx.fillStyle = "#7a9db0";
          ctx.font = "11px 'Courier New'";
          ctx.fillText("FLY ACROSS  3  PARALLEL  UNIVERSES", W / 2, H / 2 - 4);

          // Dim indicators
          DIMS.forEach((d, i) => {
            const bx = W / 2 - 70 + i * 70;
            ctx.fillStyle = d.color;
            ctx.shadowColor = d.color; ctx.shadowBlur = 12;
            ctx.fillRect(bx - 20, H / 2 + 20, 40, 6);
            ctx.shadowBlur = 0;
            ctx.font = "10px 'Courier New'";
            ctx.fillStyle = d.color;
            ctx.fillText(d.name, bx, H / 2 + 44);
          });

          ctx.fillStyle = "#fff";
          ctx.font = "bold 14px 'Courier New'";
          ctx.fillText("TAP / SPACE  to Begin", W / 2, H / 2 + 80);
          ctx.fillStyle = "#4a6a7a";
          ctx.font = "11px 'Courier New'";
          ctx.fillText("← → / Q E  :  Shift Dimension", W / 2, H / 2 + 108);
          ctx.fillText("SPACE / TAP  :  Flap", W / 2, H / 2 + 126);
        }

        if (phase === "dead") {
          ctx.textAlign = "center";
          ctx.fillStyle = "#ff2d78";
          ctx.shadowColor = "#ff2d78"; ctx.shadowBlur = 30;
          ctx.font = "bold 32px 'Courier New'";
          ctx.fillText("RIFT  COLLAPSED", W / 2, H / 2 - 70);
          ctx.shadowBlur = 0;

          const dc = DIMS[hi.dim].color;
          ctx.fillStyle = "#fff";
          ctx.font = "bold 52px 'Courier New'";
          ctx.shadowColor = dc; ctx.shadowBlur = 20;
          ctx.fillText(hi.score, W / 2, H / 2);
          ctx.shadowBlur = 0;

          ctx.fillStyle = "#7a9db0";
          ctx.font = "12px 'Courier New'";
          ctx.fillText("SCORE", W / 2, H / 2 + 22);

          ctx.fillStyle = "#ffcc00";
          ctx.font = "bold 20px 'Courier New'";
          ctx.shadowColor = "#ffcc00"; ctx.shadowBlur = 10;
          ctx.fillText(`BEST   ${best}`, W / 2, H / 2 + 56);
          ctx.shadowBlur = 0;

          ctx.fillStyle = "#4a6a7a";
          ctx.font = "13px 'Courier New'";
          ctx.fillText("TAP / SPACE  to Restart", W / 2, H / 2 + 100);
        }

        raf.current = requestAnimationFrame(loop);
        return;
      }

      const b = g.bird;

      // ── UPDATE ──────────────────────────────────────────────────────────────

      // Spawn pipes (one per dim, same x, random independent gapY)
      g.timer++;
      if (g.timer % SPAWN_INTERVAL === 0) {
        for (let d = 0; d < 3; d++) {
          g.pipes[d].push({ x: W + 20, gapY: newGapY(), passed: false });
        }
        g.speed = Math.min(BASE_SPEED + g.score * 0.03, 5.5);
      }

      // Move + cull pipes
      for (let d = 0; d < 3; d++) {
        g.pipes[d].forEach(p => { p.x -= g.speed; });
        g.pipes[d] = g.pipes[d].filter(p => p.x > -PIPE_W - 20);
      }

      // Physics
      b.vy += GRAV;
      b.vy = Math.min(b.vy, 13);
      b.y += b.vy;
      b.angle = Math.min(b.angle + 0.04, 1.1);
      if (b.shiftCD > 0) b.shiftCD--;

      // Score pass check (current dim)
      g.pipes[b.dim].forEach(p => {
        if (!p.passed && p.x + PIPE_W < BIRD_X - BIRD_R) {
          p.passed = true;
          g.score++;
          g.combo++;
          g.comboTimer = 80;
          setScore(g.score);
          // Score text particle
          g.particles.push({ x: BIRD_X + 30, y: b.y - 20, vx: 1.5, vy: -1.8, life: 35, maxLife: 35, color: DIMS[b.dim].color, sz: 0, isText: true, text: g.combo > 1 ? `+${g.combo}` : "+1" });
        }
      });
      if (g.comboTimer > 0) g.comboTimer--;
      else g.combo = 0;

      // Collision — current dim pipes only
      let dead = false;
      const bL = BIRD_X - BIRD_R + 4, bR = BIRD_X + BIRD_R - 4;
      const bT = b.y - BIRD_R + 4, bBot = b.y + BIRD_R - 4;

      g.pipes[b.dim].forEach(p => {
        if (bR > p.x + 4 && bL < p.x + PIPE_W - 4) {
          if (bT < p.gapY - GAP / 2 || bBot > p.gapY + GAP / 2) dead = true;
        }
      });

      if (b.y + BIRD_R > H || b.y - BIRD_R < 0) dead = true;

      if (dead && !b.dead) {
        b.dead = true;
        for (let i = 0; i < 30; i++) {
          const a = (i / 30) * Math.PI * 2;
          g.particles.push({ x: BIRD_X, y: b.y, vx: Math.cos(a) * rng(3, 7), vy: Math.sin(a) * rng(3, 7), life: 40, maxLife: 40, color: DIMS[b.dim].color, sz: rng(3, 7) });
        }
        const finalScore = g.score;
        const finalDim = b.dim;
        setBest(prev => Math.max(prev, finalScore));
        setHi({ score: finalScore, dim: finalDim });
        setTimeout(() => setPhase("dead"), 500);
      }

      // Trail
      g.trail.unshift({ x: BIRD_X, y: b.y, dim: b.dim });
      if (g.trail.length > 24) g.trail.pop();

      // Particles & ripples
      g.particles = g.particles.filter(p => p.life > 0);
      g.particles.forEach(p => { p.x += p.vx; p.y += p.vy; p.vy += 0.05; p.life--; });
      g.ripples = g.ripples.filter(r => r.life > 0);
      g.ripples.forEach(r => { r.r += 5; r.life--; });
      if (g.flash > 0) g.flash--;

      // ── RENDER ──────────────────────────────────────────────────────────────

      // Dim flash
      if (g.flash > 0) {
        ctx.fillStyle = g.flashColor + Math.floor((g.flash / 12) * 35).toString(16).padStart(2, "0");
        ctx.fillRect(0, 0, W, H);
      }

      // Subtle dim-tint bg
      ctx.fillStyle = DIMS[b.dim].bg;
      ctx.fillRect(0, 0, W, H);

      // Grid
      ctx.strokeStyle = `${DIMS[b.dim].color}08`;
      ctx.lineWidth = 1;
      for (let x = 0; x < W; x += 40) { ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, H); ctx.stroke(); }
      for (let y = 0; y < H; y += 40) { ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(W, y); ctx.stroke(); }

      // Ghost pipes (other dims)
      for (let d = 0; d < 3; d++) {
        if (d === b.dim) continue;
        g.pipes[d].forEach(p => drawPipe(p.x, p.gapY, d, 0.1));
      }

      // Active dim pipes
      g.pipes[b.dim].forEach(p => drawPipe(p.x, p.gapY, b.dim, 1));

      // Ripples
      g.ripples.forEach(r => {
        ctx.globalAlpha = r.life / 20 * 0.5;
        ctx.strokeStyle = r.color;
        ctx.lineWidth = 2;
        ctx.shadowColor = r.color; ctx.shadowBlur = 10;
        ctx.beginPath(); ctx.arc(r.x, r.y, r.r, 0, Math.PI * 2); ctx.stroke();
        ctx.shadowBlur = 0; ctx.globalAlpha = 1;
      });

      // Trail
      g.trail.forEach((pt, i) => {
        const a = (1 - i / g.trail.length) * 0.45;
        const sz = BIRD_R * 0.6 * (1 - i / g.trail.length);
        ctx.globalAlpha = a;
        ctx.fillStyle = DIMS[pt.dim].color;
        ctx.shadowColor = DIMS[pt.dim].color; ctx.shadowBlur = 8;
        ctx.beginPath(); ctx.arc(pt.x, pt.y, sz, 0, Math.PI * 2); ctx.fill();
        ctx.shadowBlur = 0;
      });
      ctx.globalAlpha = 1;

      // Bird (skip if dead)
      if (!b.dead) drawBird(BIRD_X, b.y, b.dim, b.angle);

      // Particles
      g.particles.forEach(p => {
        ctx.globalAlpha = p.life / p.maxLife;
        if (p.isText) {
          ctx.fillStyle = p.color;
          ctx.shadowColor = p.color; ctx.shadowBlur = 10;
          ctx.font = `bold 16px 'Courier New'`;
          ctx.textAlign = "center";
          ctx.fillText(p.text, p.x, p.y);
          ctx.shadowBlur = 0;
        } else {
          ctx.fillStyle = p.color;
          ctx.shadowColor = p.color; ctx.shadowBlur = 10;
          ctx.beginPath(); ctx.arc(p.x, p.y, p.sz, 0, Math.PI * 2); ctx.fill();
          ctx.shadowBlur = 0;
        }
        ctx.globalAlpha = 1;
      });

      // ── HUD ─────────────────────────────────────────────────────────────────
      // Score
      ctx.textAlign = "center";
      ctx.fillStyle = "#fff";
      ctx.shadowColor = DIMS[b.dim].color; ctx.shadowBlur = 16;
      ctx.font = "bold 38px 'Courier New'";
      ctx.fillText(g.score, W / 2, 52);
      ctx.shadowBlur = 0;

      // Combo
      if (g.combo > 1 && g.comboTimer > 0) {
        ctx.fillStyle = "#ffcc00";
        ctx.shadowColor = "#ffcc00"; ctx.shadowBlur = 12;
        ctx.font = `bold 13px 'Courier New'`;
        ctx.fillText(`× ${g.combo} COMBO`, W / 2, 74);
        ctx.shadowBlur = 0;
      }

      // Dimension bar (bottom)
      const barY = H - 52;
      ctx.fillStyle = "rgba(0,0,0,0.5)";
      ctx.beginPath(); ctx.roundRect(W / 2 - 80, barY, 160, 32, 6); ctx.fill();

      DIMS.forEach((d, i) => {
        const bx = W / 2 - 64 + i * 52;
        const active = i === b.dim;
        ctx.fillStyle = active ? d.color + "33" : "transparent";
        ctx.beginPath(); ctx.roundRect(bx - 18, barY + 4, 36, 24, 4); ctx.fill();
        ctx.shadowColor = active ? d.color : "transparent";
        ctx.shadowBlur = active ? 10 : 0;
        ctx.fillStyle = active ? d.color : d.color + "55";
        ctx.font = `${active ? "bold " : ""}9px 'Courier New'`;
        ctx.textAlign = "center";
        ctx.fillText(d.name, bx, barY + 20);
        ctx.shadowBlur = 0;
      });

      // Shift cooldown bar
      if (b.shiftCD > 0) {
        ctx.fillStyle = "rgba(255,255,255,0.15)";
        ctx.beginPath(); ctx.roundRect(W / 2 - 40, barY + 38, 80, 3, 2); ctx.fill();
        ctx.fillStyle = DIMS[b.dim].color;
        ctx.beginPath(); ctx.roundRect(W / 2 - 40, barY + 38, 80 * (1 - b.shiftCD / 22), 3, 2); ctx.fill();
      }

      // Speed indicator (top right)
      ctx.textAlign = "right";
      ctx.fillStyle = "#3a5a6a";
      ctx.font = "10px 'Courier New'";
      ctx.fillText(`${g.speed.toFixed(1)}×`, W - 14, 20);

      raf.current = requestAnimationFrame(loop);
    }

    raf.current = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(raf.current);
  }, [phase, best, hi]);

  // ── Touch shift ──────────────────────────────────────────────────────────────
  const touchStart = useRef(null);
  const handleTouchStart = (e) => { touchStart.current = { x: e.touches[0].clientX, y: e.touches[0].clientY, t: Date.now() }; };
  const handleTouchEnd = (e) => {
    if (!touchStart.current) return;
    const dx = e.changedTouches[0].clientX - touchStart.current.x;
    const dy = e.changedTouches[0].clientY - touchStart.current.y;
    const dt = Date.now() - touchStart.current.t;
    if (Math.abs(dx) > 35 && Math.abs(dx) > Math.abs(dy) && dt < 400) {
      shiftDim(dx > 0 ? 1 : -1);
    } else {
      tap();
    }
    touchStart.current = null;
  };

  const borderColor = DIMS[dim]?.color ?? "#00f5ff";

  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", minHeight: "100vh", background: "#010205", userSelect: "none" }}>
      <canvas
        ref={cvs}
        width={W} height={H}
        style={{ maxWidth: "100vw", maxHeight: "95vh", objectFit: "contain", cursor: "pointer", border: `1.5px solid ${borderColor}33`, boxShadow: `0 0 60px ${borderColor}18, 0 0 120px ${borderColor}08` }}
        onClick={tap}
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
      />
      <div style={{ marginTop: 10, fontFamily: "monospace", fontSize: 10, color: "#2a4a5a", letterSpacing: 2 }}>
        RIFT WINGS — SWIPE ← → TO SHIFT DIMENSION
      </div>
    </div>
  );
}

The code below contains a design. This design should be used to create a new app or be added to an existing one.

Look at the current open project to determine if a project exists. If no project is open, create a new Vite project then create this view in React after componentizing it.

If a project does exist, determine the framework being used and implement the design within that framework. Identify whether reusable components already exist that can be used to implement the design faithfully and if so use them, otherwise create new components. If other views already exist in the project, make sure to place the view in a sensible route and connect it to the other views.

Ensure the visual characteristics, layout, and interactions in the design are preserved with perfect fidelity.

Run the dev command so the user can see the app once finished.

```
<html lang="en" vid="0"><head vid="1">
    <meta charset="UTF-8" vid="2">
    <meta name="viewport" content="width=device-width, initial-scale=1.0" vid="3">
    <title vid="4">PANZER-BIT // SYSTEM-01</title>
    <style vid="5">
        @import url('https://fonts.googleapis.com/css2?family=VT323&display=swap');

        :root {
            --bg: #eff3f1;
            --fg: #000000;
            --pixel: 2px;
        }

        * {
            box-sizing: border-box;
            cursor: url('data:image/svg+xml;utf8,<svg width="20" height="20" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><rect x="9" y="0" width="2" height="20" fill="black"/><rect x="0" y="9" width="20" height="2" fill="black"/></svg>') 10 10, auto;
        }

        body {
            margin: 0;
            padding: 20px;
            background-color: var(--bg);
            color: var(--fg);
            font-family: 'VT323', monospace;
            text-transform: uppercase;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            overflow: hidden;
            image-rendering: pixelated;
        }


        .dither-bg {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: -1;
            background-image: radial-gradient(circle, var(--fg) 1px, transparent 1px);
            background-size: 4px 4px;
            opacity: 0.05;
        }

        .main-frame {
            width: 900px;
            height: 600px;
            border: 4px solid var(--fg);
            outline: 1px solid var(--fg);
            outline-offset: 4px;
            padding: 4px;
            display: grid;
            grid-template-columns: 240px 1fr 200px;
            grid-template-rows: auto 1fr auto;
            gap: 12px;
            position: relative;
            background: var(--bg);
        }


        .header {
            grid-column: 1 / -1;
            border-bottom: 2px solid var(--fg);
            padding-bottom: 8px;
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
        }

        .title-block h1 {
            margin: 0;
            font-size: 48px;
            line-height: 0.8;
            letter-spacing: -2px;
        }

        .system-status {
            font-size: 14px;
            text-align: right;
            line-height: 1;
        }


        .sidebar {
            border-right: 1px solid var(--fg);
            padding-right: 12px;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .nav-item {
            border: 2px solid var(--fg);
            padding: 8px 12px;
            font-size: 24px;
            text-decoration: none;
            color: var(--fg);
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: none;
        }

        .nav-item:hover {
            background-color: var(--fg);
            color: var(--bg);
        }

        .nav-item::before {
            content: "□";
            margin-right: 10px;
        }

        .nav-item:hover::before {
            content: "■";
        }


        .canvas-area {
            position: relative;
            display: flex;
            justify-content: center;
            align-items: center;
            overflow: hidden;
            border: 1px dashed var(--fg);
        }

        .tank-art {
            width: 300px;
            height: 300px;
            position: relative;
        }


        .tank-shadow {
            position: absolute;
            bottom: 40px;
            left: 50%;
            transform: translateX(-50%);
            width: 220px;
            height: 40px;
            background-image:
                conic-gradient(from 0deg at 50% 50%, var(--fg) 25%, transparent 0) 0 0/4px 4px,
                conic-gradient(from 0deg at 50% 50%, var(--fg) 25%, transparent 0) 2px 2px/4px 4px;
            opacity: 0.4;
        }


        .info-panel {
            border-left: 1px solid var(--fg);
            padding-left: 12px;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .stat-box {
            border: 1px solid var(--fg);
            padding: 6px;
        }

        .stat-label {
            font-size: 12px;
            border-bottom: 1px solid var(--fg);
            margin-bottom: 4px;
            display: block;
        }

        .bar-container {
            width: 100%;
            height: 12px;
            border: 1px solid var(--fg);
            margin-top: 4px;
            position: relative;
        }

        .bar-fill {
            height: 100%;
            background: var(--fg);
            width: 70%;
        }

        .bar-fill.dithered {
            background-image:
                repeating-linear-gradient(45deg, var(--fg), var(--fg) 2px, transparent 2px, transparent 4px);
        }


        .footer {
            grid-column: 1 / -1;
            border-top: 2px solid var(--fg);
            padding-top: 8px;
            display: flex;
            justify-content: space-between;
            font-size: 14px;
        }


        .corner-bracket {
            position: absolute;
            width: 15px;
            height: 15px;
            border: 2px solid var(--fg);
        }
        .top-left { top: -10px; left: -10px; border-right: 0; border-bottom: 0; }
        .top-right { top: -10px; right: -10px; border-left: 0; border-bottom: 0; }
        .bottom-left { bottom: -10px; left: -10px; border-right: 0; border-top: 0; }
        .bottom-right { bottom: -10px; right: -10px; border-left: 0; border-top: 0; }


        @keyframes flicker {
            0% { opacity: 1; }
            5% { opacity: 0.95; }
            10% { opacity: 1; }
            15% { opacity: 0.9; }
            20% { opacity: 1; }
        }

        .main-frame {
            animation: flicker 4s infinite step-end;
        }


        .pixel-tank {
            width: 100%;
            height: 100%;
            fill: var(--fg);
        }
    </style>
</head>
<body vid="6">

<div class="dither-bg" vid="7"></div>

<div class="main-frame" vid="8">
    <div class="corner-bracket top-left" vid="9"></div>
    <div class="corner-bracket top-right" vid="10"></div>
    <div class="corner-bracket bottom-left" vid="11"></div>
    <div class="corner-bracket bottom-right" vid="12"></div>

    <header class="header" vid="13">
        <div class="title-block" vid="14">
            <span style="font-size: 12px; letter-spacing: 2px;" vid="15">HEAVY ARMORED DIVISION</span>
            <h1 vid="16">PANZER-BIT</h1>
        </div>
        <div class="system-status" vid="17">
            LOC: SECTOR_G4<br vid="18">
            NET: ENCRYPTED<br vid="19">
            VER: 1.0.4-STABLE
        </div>
    </header>

    <nav class="sidebar" vid="20">
        <a href="#" class="nav-item" vid="21">DEPLOY UNIT<span vid="22">01</span></a>
        <a href="#" class="nav-item" vid="23">ARMORY<span vid="24">02</span></a>
        <a href="#" class="nav-item" vid="25">STRATEGY<span vid="26">03</span></a>
        <a href="#" class="nav-item" vid="27">SETTINGS<span vid="28">04</span></a>
        <a href="#" class="nav-item" vid="29">TERMINATE<span vid="30">05</span></a>

        <div style="margin-top: auto; border: 1px solid var(--fg); padding: 8px; font-size: 12px;" vid="31">
            <div style="display: flex; justify-content: space-between;" vid="32">
                <span vid="33">CPU_LOAD</span>
                <span vid="34">42%</span>
            </div>
            <div class="bar-container" vid="35"><div class="bar-fill dithered" style="width: 42%;" vid="36"></div></div>
        </div>
    </nav>

    <main class="canvas-area" vid="37">
        <div class="tank-shadow" vid="38"></div>
        <div class="tank-art" vid="39">

            <svg class="pixel-tank" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg" shape-rendering="crispEdges" vid="40">

                <rect x="10" y="44" width="44" height="10" vid="41"></rect>
                <rect x="12" y="42" width="40" height="2" vid="42"></rect>

                <rect x="14" y="46" width="4" height="6" fill="#eff3f1" vid="43"></rect>
                <rect x="22" y="46" width="4" height="6" fill="#eff3f1" vid="44"></rect>
                <rect x="30" y="46" width="4" height="6" fill="#eff3f1" vid="45"></rect>
                <rect x="38" y="46" width="4" height="6" fill="#eff3f1" vid="46"></rect>
                <rect x="46" y="46" width="4" height="6" fill="#eff3f1" vid="47"></rect>

                <rect x="14" y="34" width="36" height="10" vid="48"></rect>
                <rect x="18" y="32" width="28" height="2" vid="49"></rect>

                <rect x="22" y="24" width="20" height="8" vid="50"></rect>
                <rect x="24" y="22" width="16" height="2" vid="51"></rect>

                <rect x="42" y="26" width="18" height="4" vid="52"></rect>
                <rect x="58" y="25" width="2" height="6" vid="53"></rect>

                <rect x="26" y="20" width="8" height="2" vid="54"></rect>

                <rect x="24" y="24" width="2" height="2" fill="#eff3f1" vid="55"></rect>
                <rect x="26" y="26" width="2" height="2" fill="#eff3f1" vid="56"></rect>
            </svg>
        </div>


        <div style="position: absolute; top: 10px; right: 10px; text-align: right;" vid="57">
            <div style="font-size: 10px;" vid="58">SCANNING...</div>
            <div style="width: 40px; height: 1px; background: var(--fg); margin: 2px 0 2px auto;" vid="59"></div>
            <div style="font-size: 10px;" vid="60">ID: TKN-88</div>
        </div>

        <div style="position: absolute; bottom: 20px; left: 20px; font-size: 10px; width: 120px;" vid="61">
            <div style="border-bottom: 1px solid var(--fg);" vid="62">ENGINE_CORE</div>
            [|||||||||||.....]
        </div>
    </main>

    <aside class="info-panel" vid="63">
        <div class="stat-box" vid="64">
            <span class="stat-label" vid="65">OFFENSIVE POWER</span>
            <div style="font-size: 24px; font-weight: bold;" vid="66">88.4</div>
            <div class="bar-container" vid="67"><div class="bar-fill" style="width: 88%;" vid="68"></div></div>
        </div>

        <div class="stat-box" vid="69">
            <span class="stat-label" vid="70">ARMOR DENSITY</span>
            <div style="font-size: 24px; font-weight: bold;" vid="71">62.1</div>
            <div class="bar-container" vid="72"><div class="bar-fill dithered" style="width: 62%;" vid="73"></div></div>
        </div>

        <div class="stat-box" vid="74">
            <span class="stat-label" vid="75">MOBILITY INDEX</span>
            <div style="font-size: 24px; font-weight: bold;" vid="76">45.9</div>
            <div class="bar-container" vid="77"><div class="bar-fill" style="width: 45%;" vid="78"></div></div>
        </div>

        <div style="margin-top: auto; font-size: 10px; line-height: 1.2;" vid="79">
            <div style="border-bottom: 1px solid var(--fg); margin-bottom: 4px;" vid="80">SYSTEM LOGS</div>
            &gt; CALIBRATING OPTICS<br vid="81">
            &gt; FUEL CELL: OPTIMAL<br vid="82">
            &gt; RADAR: ACTIVE
        </div>
    </aside>

    <footer class="footer" vid="83">
        <div vid="84">CODENAME: B-SIDE TACTICAL</div>
        <div vid="85">(C) 198X HEAVY_DATA_INDUSTRIES</div>
        <div vid="86">PRESS [START] TO INITIALIZE</div>
    </footer>
</div>

<script vid="87">

    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('mouseenter', () => {
            console.log('System focus: ' + item.innerText);
        });
    });


    setInterval(() => {
        const time = new Date().toLocaleTimeString('en-GB', { hour12: false });
        document.querySelector('.system-status').innerHTML = `
            LOC: SECTOR_G4<br>
            NET: ENCRYPTED<br>
            TME: ${time}
        `;
    }, 1000);
</script>


</body></html>
```

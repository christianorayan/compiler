// Using global mermaid from CDN

// Initialize Mermaid with monochrome theme
mermaid.initialize({
    startOnLoad: false,
    theme: 'base',
    themeVariables: {
        primaryColor: '#000000',
        primaryTextColor: '#ffffff',
        primaryBorderColor: '#ffffff',
        lineColor: '#ffffff',
        secondaryColor: '#000000',
        tertiaryColor: '#000000',
        arrowheadColor: '#ffffff',
        edgeLabelBackground: '#000000',
        fontSize: '14px',
        fontFamily: 'JetBrains Mono'
    },
    securityLevel: 'loose',
    flowchart: {
        useMaxWidth: false,
        htmlLabels: true,
        curve: 'linear'
    }
});

let currentData = null;

// UI Elements
const runBtn = document.getElementById('run-btn');
const codeInput = document.getElementById('code-input');
const originalCode = document.getElementById('original-code');
const optimizedCode = document.getElementById('optimized-code');
const consoleMsg = document.getElementById('console-msg');
const mermaidGraph = document.getElementById('mermaid-graph');

// Tab Logic
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const tabId = btn.getAttribute('data-tab');
        const cfgId = btn.getAttribute('data-cfg');

        if (tabId) {
            document.querySelectorAll('.tab-btn:not(.sub-tab)').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(tabId).classList.add('active');
            log(`ACTION: SWITCH_TO_${tabId.replace('-', '_').toUpperCase()}`);
        }

        if (cfgId) {
            document.querySelectorAll('.sub-tab').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            renderCFG(cfgId);
        }
    });
});

async function runOptimizer() {
    const code = codeInput.value.trim();
    if (!code) {
        log('ERROR: NO_INPUT_DETECTED', 'error');
        return;
    }

    log('STATUS: ESTABLISHING_CONNECTION...');
    runBtn.disabled = true;
    runBtn.innerText = '>>> PROCESSING...';

    try {
        const response = await fetch('http://localhost:3001/api/compile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code })
        });

        const data = await response.json();
        
        if (data.error) {
            log(`COMPILER_ERROR: ${data.error}`, 'error');
            return;
        }

        currentData = data;
        displayResults(data);
        log('SUCCESS: OPTIMIZATION_SEQUENCE_COMPLETE');

    } catch (err) {
        log(`CRITICAL_FAILURE: ${err.message}`, 'error');
    } finally {
        runBtn.disabled = false;
        runBtn.innerText = 'RUN_OPTIMIZER';
    }
}

function displayResults(data) {
    originalCode.innerText = data.original.join('\n');
    optimizedCode.innerText = data.optimized.join('\n');
    
    // Default to initial CFG view
    renderCFG('initial');
}

async function renderCFG(type) {
    if (!currentData) return;
    
    const cfg = type === 'initial' ? currentData.initialCfg : currentData.finalCfg;
    const dot = convertToMermaid(cfg);
    
    // Clear and prepare container
    mermaidGraph.innerHTML = '';
    const graphDiv = document.createElement('div');
    graphDiv.className = 'mermaid';
    graphDiv.id = 'tg-' + Date.now();
    mermaidGraph.appendChild(graphDiv);

    try {
        const { svg } = await mermaid.render(graphDiv.id + '-svg', dot);
        graphDiv.innerHTML = svg;
    } catch (e) {
        console.error('Mermaid error:', e);
        graphDiv.innerHTML = `<div style="color: #ffffff; background: #000000; border: 2px solid #ffffff; padding: 20px;">
            GRAPH_RENDER_FAILED. CHECK_CONSOLE.<br/>
            <pre style="font-size: 10px; margin-top: 10px;">${dot}</pre>
        </div>`;
    }
}

function convertToMermaid(cfg) {
    let m = 'graph TD\n';
    m += '  classDef default fill:#000000,stroke:#ffffff,color:#ffffff,font-family:JetBrains Mono,stroke-width:2px;\n';
    m += '  classDef block fill:#000000,stroke:#ffffff,color:#ffffff,stroke-width:3px;\n';
    
    cfg.blocks.forEach(block => {
        // Sanitize instructions for Mermaid labels:
        // 1. Replace " with ' to avoid breaking the label string
        // 2. Escape or remove other problematic characters like [], (), {}
        const insts = block.instructions.map(i => 
            i.replace(/"/g, "'") // Handle quotes
             .replace(/[\[\]\(\){}]/g, '') // Stripping brackets
             .replace(/#/g, 'HASH_') // Strip hash
        ).join('<br/>');
        
        const label = `"BLOCK_${block.id}<br/>${insts}"`;
        
        m += `  B${block.id}[${label}]\n`;
        m += `  class B${block.id} block\n`;
    });

    cfg.blocks.forEach(block => {
        block.succs.forEach(succId => {
            m += `  B${block.id} --> B${succId}\n`;
        });
    });

    return m;
}

function log(msg, type = 'info') {
    const time = new Date().toLocaleTimeString([], { hour12: false });
    consoleMsg.innerText = `[${time}] ${msg}`;
    if (type === 'error') {
        consoleMsg.style.fontWeight = '900';
        consoleMsg.style.textDecoration = 'underline';
    } else {
        consoleMsg.style.fontWeight = 'normal';
        consoleMsg.style.textDecoration = 'none';
    }
}

runBtn.addEventListener('click', runOptimizer);

// Initial log
log('SYSTEM_READY. KERNEL_LOADED: COMPILER_LAB');

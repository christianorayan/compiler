const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const { exec, spawn } = require('child_process');
const path = require('path');
const fs = require('fs');

const app = express();
const port = 3001;

app.use(cors());
app.use(bodyParser.json());

const PROJECT_ROOT = path.join(__dirname, '..', '..');
const BIN_DIR = path.join(PROJECT_ROOT, 'bin');
const SRC_DIR = path.join(PROJECT_ROOT, 'src');

app.post('/api/compile', (req, res) => {
    const { code } = req.body;
    
    // Compile Java code first (just in case it was changed)
    // In a production app, we'd compile once and just run the byte code.
    exec(`javac -d bin -sourcepath src src/main/WebMain.java`, { cwd: PROJECT_ROOT }, (error, stdout, stderr) => {
        if (error) {
            console.error(`Compilation error: ${stderr}`);
            return res.status(500).json({ error: stderr });
        }

        // Run WebMain and pipe code to its stdin
        const javaProcess = spawn('java', ['-cp', 'bin', 'main.WebMain'], { cwd: PROJECT_ROOT });
        
        let output = '';
        let errorOutput = '';

        javaProcess.stdin.write(code);
        javaProcess.stdin.end();

        javaProcess.stdout.on('data', (data) => {
            output += data.toString();
        });

        javaProcess.stderr.on('data', (data) => {
            errorOutput += data.toString();
        });

        javaProcess.on('close', (code) => {
            if (code !== 0) {
                return res.status(500).json({ error: errorOutput });
            }
            try {
                const result = JSON.parse(output);
                res.json(result);
            } catch (e) {
                res.status(500).json({ error: "Failed to parse JSON output", raw: output });
            }
        });
    });
});

app.listen(port, () => {
    console.log(`Backend listening at http://localhost:${port}`);
});

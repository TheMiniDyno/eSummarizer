let typingTimeouts = [];
let isClearing = false;

// Function to summarize the input text
async function summarizeText() {
    const text = document.getElementById('inputText').value;

    // Check if the input is empty
    if (text.trim() === '') {
        alert('Please enter some text to summarize.');
        return;
    }

    try {
        const response = await fetch('/summarize', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: text
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const summaryInfo = await response.json();

        // display the summary
        let cleanedSummary = summaryInfo.summarizedText;

        // Clear any existing typewriter animations before starting a new one
        clearAllTimeouts();
        isClearing = false;
        document.getElementById('summaryText').value = ''; // Clear the textarea before starting the animation
        typeWriter(cleanedSummary, document.getElementById('summaryText'), 0, 0.1);

        // Update statistics
        document.getElementById('originalSentences').textContent = summaryInfo.originalSentenceCount;
        document.getElementById('summarizedSentences').textContent = summaryInfo.summarizedSentenceCount;
        document.getElementById('originalWords').textContent = summaryInfo.originalWordCount;
        document.getElementById('summarizedWords').textContent = summaryInfo.summarizedWordCount;
        document.getElementById('reductionRate').textContent = (summaryInfo.reductionRate * 100).toFixed(2) + '%';

    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred while summarizing the text. Please try again.');
    }
}

// Function to type text letter by letter
function typeWriter(text, textArea, index, speed) {
    if (isClearing) {
        return;
    }

    if (index < text.length) {
        textArea.value += text.charAt(index);
        index++;
        const timeoutId = setTimeout(function () {
            typeWriter(text, textArea, index, speed);
        }, speed);
        typingTimeouts.push(timeoutId);
    }
}

// Function to clear all timeouts
function clearAllTimeouts() {
    for (const timeoutId of typingTimeouts) {
        clearTimeout(timeoutId);
    }
    typingTimeouts = [];
}

// Optional: Add event listener for Enter key in input textarea
document.getElementById('inputText').addEventListener('keydown', function (event) {
    if (event.key === 'Enter' && event.ctrlKey) {
        event.preventDefault(); // Prevent default action
        summarizeText(); // Call the summarize function
    }
});

// Optional: Add function to clear input and results
function clearAll() {
    isClearing = true;
    clearAllTimeouts();
    document.getElementById('inputText').value = '';
    document.getElementById('summaryText').value = '';
    document.getElementById('originalSentences').textContent = '';
    document.getElementById('summarizedSentences').textContent = '';
    document.getElementById('originalWords').textContent = '';
    document.getElementById('summarizedWords').textContent = '';
    document.getElementById('reductionRate').textContent = '';
}


//..............footer
document.addEventListener('DOMContentLoaded', function () {
    const yearSpan = document.getElementById('year');
    const currentYear = new Date().getFullYear();
    yearSpan.textContent = currentYear;
});
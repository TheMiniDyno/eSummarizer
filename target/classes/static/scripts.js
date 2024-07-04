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

        // Display the summary
        document.getElementById('summaryText').value = summaryInfo.summarizedText.join('\n');

        // Update statistics
        document.getElementById('originalSentences').textContent = summaryInfo.originalSentenceCount;
        document.getElementById('summarizedSentences').textContent = summaryInfo.summarizedSentenceCount;
        document.getElementById('originalWords').textContent = summaryInfo.originalWordCount;
        document.getElementById('summarizedWords').textContent = summaryInfo.summarizedWordCount;
        document.getElementById('reductionRate').textContent = (summaryInfo.reductionRate * 100).toFixed(2) + '%';

        // Show statistics (they're always visible now, so we don't need to change display)
        // Uncomment the next line if you want to show/hide the entire stats section
        // document.querySelector('.stats').style.display = 'block';

    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred while summarizing the text. Please try again.');
    }
}

// Optional: Add event listener for Enter key in input textarea
document.getElementById('inputText').addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && event.ctrlKey) {
        event.preventDefault(); // Prevent default action
        summarizeText(); // Call the summarize function
    }
});

// Optional: Add function to clear input and results
function clearAll() {
    document.getElementById('inputText').value = '';
    document.getElementById('summaryText').value = '';
    document.getElementById('originalSentences').textContent = '';
    document.getElementById('summarizedSentences').textContent = '';
    document.getElementById('originalWords').textContent = '';
    document.getElementById('summarizedWords').textContent = '';
    document.getElementById('reductionRate').textContent = '';
}

// Optional: Add this line if you want to show statistics only after summarization
// document.querySelector('.stats').style.display = 'none';
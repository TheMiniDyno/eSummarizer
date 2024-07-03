    async function summarizeText() {
    const text = document.getElementById('inputText').value;
    const response = await fetch('/summarize', {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json'
},
    body: JSON.stringify(text)
});
    const summary = await response.json();
    document.getElementById('summaryText').value = summary.join('\n');
}

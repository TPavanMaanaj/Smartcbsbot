(function() {
    'use strict';

    // Configuration
    const CONFIG = {
        apiEndpoint: 'https://smartbot.smartcbs.net/api/chat',
        botName: 'SmartBot Assistant',
        botSubtitle: 'SmartCBS Banking Platform',
        welcomeMessage: "Hello! I'm SmartBot Assistant from SmartCBS. How can I help you today?"
    };

    // Load marked.js for Markdown support
    if (typeof marked === 'undefined') {
        const markedScript = document.createElement('script');
        markedScript.src = 'https://cdn.jsdelivr.net/npm/marked/marked.min.js';
        markedScript.onload = () => {
            // Optional: Configure marked if needed
            // marked.setOptions({ breaks: true });
        };
        document.head.appendChild(markedScript);
    }

    // Inject CSS
    const style = document.createElement('style');
    style.textContent = `
        :root {
            --sb-primary:rgb(247, 20, 20);
            --sb-bg-light: #ffffff;
            --sb-bg-gray: #f9fafb;
            --sb-border: #e5e7eb;
            --sb-text-main: #111827;
            --sb-text-sub: #6b7280;
        }

        #smartbot-widget {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            position: fixed;
            bottom: 16px;
            right: 16px;
            z-index: 999999;
            display: flex;
            flex-direction: column;
            align-items: flex-end;
        }

        /* Toggle Button - Smaller */
        #sb-toggle-btn {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            padding:0;
            box-sizing: border-box;  
            background-color: var(--sb-primary);
            box-shadow: 0 3px 8px rgba(26, 86, 219, 0.3);
            border: none;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: transform 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            color: white;
            font-size: 18px;
            line-height: 1;
            flex-shrink: 0; 
        }

        #sb-toggle-btn:hover {
            transform: scale(1.05);
        }

        /* Chat Window - Smaller */
        #sb-window {
            width: 320px;
            height: 460px;
            background-color: var(--sb-bg-light);
            border-radius: 12px;
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
            margin-bottom: 16px;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            opacity: 0;
            transform: translateY(16px) scale(0.95);
            transform-origin: bottom right;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            pointer-events: none;
            position: absolute;
            bottom: 58px;
            right: 0;
        }

        #sb-window.active {
            opacity: 1;
            transform: translateY(0) scale(1);
            pointer-events: all;
        }

        /* Header - Compact */
        .sb-header {
            background-color: var(--sb-primary);
            padding: 12px 16px;
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
        }

        .sb-header-info h3 {
            margin: 0;
            font-size: 14px;
            font-weight: 600;
            line-height: 1.2;
        }

        .sb-header-info p {
            margin: 2px 0 0;
            font-size: 11px;
            opacity: 0.9;
            line-height: 1.2;
        }

        .sb-close-btn {
            background: white;
            border: none;
            border-radius: 6px;
            width: 22px;
            height: 22px;
            color: red;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
            line-height: 1;
            padding: 0;
        }

        .sb-close-btn:hover {
            background: rgba(235, 223, 223, 0.84);
        }

        /* Messages Area - Compact */
        #sb-messages {
            flex: 1;
            padding: 16px;
            background-color: var(--sb-bg-gray);
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
            font-size: 13px;
        }

        .sb-msg-row {
            display: flex;
            align-items: flex-end;
            gap: 6px;
            max-width: 85%;
        }

        .sb-msg-row.bot {
            align-self: flex-start;
        }

        .sb-msg-row.user {
            align-self: flex-end;
            flex-direction: row-reverse;
        }

        .sb-avatar {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-size: 14px;
            line-height: 1;
        }

        .sb-avatar.bot {
            background-color: #e0e7ff;
            color: var(--sb-primary);
        }

        .sb-avatar.user {
            background-color: var(--sb-primary);
            color: white;
        }

        .sb-bubble {
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 13px;
            line-height: 1.4;
            position: relative;
            max-width: 100%;
        }

        .sb-msg-row.bot .sb-bubble {
            background-color: white;
            border: 1px solid var(--sb-border);
            color: var(--sb-text-main);
            border-bottom-left-radius: 2px;
        }

        .sb-msg-row.user .sb-bubble {
            background-color: var(--sb-primary);
            color: white;
            border-bottom-right-radius: 2px;
        }

        /* Markdown Styles - Smaller */
        .sb-bubble p {
            margin-bottom: 0.2rem;
        }
        .sb-bubble p:last-child {
            margin-bottom: 0;
        }
        .sb-bubble strong {
            font-weight: 600;
        }
        .sb-msg-row.bot .sb-bubble strong {
            color: #111827;
        }
        .sb-bubble em {
            font-style: italic;
        }
        .sb-msg-row.bot .sb-bubble em {
            color: #374151;
        }
        .sb-bubble ul {
            list-style-type: disc;
            padding-left: 1rem;
            margin: 0.2rem 0;
        }
        .sb-bubble li {
            font-size: 0.8rem;
        }
        .sb-bubble table {
            width: 100%;
            table-layout: fixed;
            border-collapse: collapse;
            border: 1px solid #d1d5db;
            font-size: 0.7rem;
            margin: 0.4rem 0;
        }
        .sb-bubble th {
            border: 1px solid #d1d5db;
            background-color: #e5e7eb;
            padding: 0.2rem 0.4rem;
            font-weight: 500;
            text-align: left;
            word-break: break-word;
            color: #111827;
        }
        .sb-bubble td {
            border: 1px solid #d1d5db;
            padding: 0.2rem 0.4rem;
            word-break: break-word;
            vertical-align: top;
        }
        .sb-bubble blockquote {
            border-left: 3px solid #60a5fa;
            padding-left: 0.6rem;
            font-style: italic;
            margin: 0.4rem 0;
            font-size: 0.85em;
        }
        .sb-msg-row.bot .sb-bubble blockquote {
            color: #4b5563;
        }
        .sb-msg-row.user .sb-bubble blockquote {
            color: rgba(255, 255, 255, 0.9);
            border-left-color: rgba(255, 255, 255, 0.5);
        }

        .sb-time {
            font-size: 9px;
            margin-top: 3px;
            text-align: right;
            opacity: 0.8;
        }
        
        .sb-msg-row.bot .sb-time {
            color: var(--sb-text-sub);
        }
        
        .sb-msg-row.user .sb-time {
            color: rgba(255, 255, 255, 0.8);
        }

        /* Typing Indicator - Smaller */
        .sb-typing {
            display: flex;
            gap: 3px;
            padding: 10px;
            background: white;
            border: 1px solid var(--sb-border);
            border-radius: 10px;
            border-bottom-left-radius: 2px;
            width: fit-content;
        }

        .sb-dot {
            width: 5px;
            height: 5px;
            background: #9ca3af;
            border-radius: 50%;
            animation: sb-bounce 1.4s infinite ease-in-out;
        }

        .sb-dot:nth-child(1) { animation-delay: -0.32s; }
        .sb-dot:nth-child(2) { animation-delay: -0.16s; }

        @keyframes sb-bounce {
            0%, 80%, 100% { transform: scale(0); }
            40% { transform: scale(1); }
        }

        /* Input Area - Compact */
        .sb-footer {
            padding: 12px;
            background-color: white;
            border-top: 1px solid var(--sb-border);
        }

        .sb-input-container {
            display: flex;
            align-items: center;
            border: 1px solid var(--sb-border);
            border-radius: 20px;
            padding: 5px 10px;
            background: white;
            gap: 6px;
            transition: border-color 0.2s;
        }

        .sb-input-container:focus-within {
            border-color: var(--sb-primary);
        }

        #sb-input {
            flex: 1;
            border: none;
            outline: none;
            padding: 6px;
            font-size: 13px;
            color: var(--sb-text-main);
            min-height: 20px;
        }

        #sb-input::placeholder {
            color: #9ca3af;
            font-size: 12.5px;
        }

        #sb-send-btn {
            background: #f3f4f6;
            border: none;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s;
            font-size: 14px;
            line-height: 1;
            color: var(--sb-primary);
            padding: 0;
        }

        #sb-send-btn:hover {
            background: var(--sb-primary);
            color: white; 
        }

        #sb-send-btn.active {
            background: var(--sb-primary);
            color: white;
        }

        .sb-powered-by {
            text-align: center;
            font-size: 10px;
            color: var(--sb-text-sub);
            margin-top: 6px;
            opacity: 0.8;
        }
        
        /* Scrollbar Styling */
        #sb-messages::-webkit-scrollbar {
            width: 4px;
        }
        
        #sb-messages::-webkit-scrollbar-track {
            background: transparent;
        }
        
        #sb-messages::-webkit-scrollbar-thumb {
            background: #cbd5e1;
            border-radius: 2px;
        }
        
        #sb-messages::-webkit-scrollbar-thumb:hover {
            background: #94a3b8;
        }
    `;
    document.head.appendChild(style);

    // Create Widget HTML
    const widget = document.createElement('div');
    widget.id = 'smartbot-widget';
    widget.innerHTML = `
        <div id="sb-window">
            <div class="sb-header">
                <div class="sb-header-info">
                    <h3 style="color: white;">${CONFIG.botName}</h3>
                    <p>${CONFIG.botSubtitle}</p>
                </div>
                <button class="sb-close-btn">×</button>
            </div>
            <div id="sb-messages">
                <!-- Messages will appear here -->
            </div>
            <div class="sb-footer">
                <div class="sb-input-container">
                    <input type="text" id="sb-input" placeholder="Type your message..." autocomplete="off">
                    <button id="sb-send-btn">➤</button>
                </div>
                <div class="sb-powered-by">Powered by SmartCBS AI</div>
            </div>
        </div>
        <button id="sb-toggle-btn">💬</button>
    `;
    document.body.appendChild(widget);

    // Elements
    const windowEl = document.getElementById('sb-window');
    const toggleBtn = document.getElementById('sb-toggle-btn');
    const closeBtn = widget.querySelector('.sb-close-btn');
    const messagesEl = document.getElementById('sb-messages');
    const inputEl = document.getElementById('sb-input');
    const sendBtn = document.getElementById('sb-send-btn');

    // Emoji Icons
    const botIcon = '🤖';
    const userIcon = '👤';

    // State
    let isOpen = false;
    let sessionId = localStorage.getItem('smartbot_session_id') || generateSessionId();

    // Generate unique session ID
    function generateSessionId() {
        const id = 'sess_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
        localStorage.setItem('smartbot_session_id', id);
        return id;
    }

    // Initialize
    function init() {
        addMessage(CONFIG.welcomeMessage, 'bot');
    }

    // Toggle Chat
    function toggleChat() {
        isOpen = !isOpen;
        if (isOpen) {
            windowEl.classList.add('active');
            inputEl.focus();
        } else {
            windowEl.classList.remove('active');
        }
    }

    toggleBtn.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleChat();
    });

    // Input Handling
    inputEl.addEventListener('input', () => {
        if (inputEl.value.trim()) {
            sendBtn.classList.add('active');
        } else {
            sendBtn.classList.remove('active');
        }
    });

    inputEl.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    sendBtn.addEventListener('click', sendMessage);

    // Send Message
    async function sendMessage() {
        const text = inputEl.value.trim();
        if (!text) return;

        // Clear input
        inputEl.value = '';
        sendBtn.classList.remove('active');

        // Add User Message
        addMessage(text, 'user');

        // Show Typing
        const typingId = showTyping();

        try {
            const response = await fetch(CONFIG.apiEndpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    message: text,
                    sessionId: sessionId 
                })
            });

            removeTyping(typingId);

            if (response.ok) {
                const data = await response.json();
                const botReply = data.response || "I don't have enough information to answer that.";
                const sources = data.sources || [];
                addMessage(botReply, 'bot', sources);
            } else {
                addMessage("I'm having trouble connecting to the server.", 'bot');
            }
        } catch (error) {
            removeTyping(typingId);
            addMessage("Sorry, something went wrong. Please check your connection.", 'bot');
        }
    }

    // Add Message
    function addMessage(text, sender, sources = []) {

    const row = document.createElement('div');
    row.className = `sb-msg-row ${sender}`;

    const avatar = document.createElement('div');
    avatar.className = `sb-avatar ${sender}`;
    avatar.textContent = sender === 'bot' ? botIcon : userIcon;
    row.appendChild(avatar);

    const bubble = document.createElement('div');
    bubble.className = `sb-bubble ${sender}`;

    const content = document.createElement('div');

    if (typeof marked !== 'undefined') {
        content.innerHTML = marked.parse(text);
    } else {
        content.textContent = text;
    }

    bubble.appendChild(content);

    /* ---------- SOURCES SECTION ---------- */

    if (sender === 'bot' && sources.length > 0) {
        // Filter out duplicate sources based on URL
        const uniqueSources = [];
        const seenUrls = new Set();
        
        sources.forEach(src => {
            const url = src.url || '';
            if (!seenUrls.has(url)) {
                seenUrls.add(url);
                uniqueSources.push(src);
            }
        });
        
        const srcContainer = document.createElement('div');
        srcContainer.style.marginTop = "8px";
        srcContainer.style.paddingTop = "6px";
        srcContainer.style.borderTop = "1px solid #e5e7eb";

        const srcTitle = document.createElement('div');
        srcTitle.style.fontWeight = "600";
        srcTitle.style.fontSize = "11px";
        srcTitle.style.color = "#6b7280";
        srcTitle.style.marginBottom = "4px";
        srcTitle.textContent = "📚 Sources:";

        const ul = document.createElement('ul');
        ul.style.margin = "0";
        ul.style.paddingLeft = "16px";
        ul.style.fontSize = "10px";

        uniqueSources.forEach((src, index) => {
            const li = document.createElement('li');
            li.style.marginBottom = "3px";
            li.style.lineHeight = "1.3";

            // SourceInfo object has: filename, sourceType, url, relevanceScore, snippet, etc.
            const fileName = src.filename || 'Unknown';
            const sourceType = src.sourceType || '';
            const url = src.url;
            const relevanceScore = src.relevanceScore ? Math.round(src.relevanceScore * 100) : null;

            if (url && url.startsWith("http")) {
                const link = document.createElement('a');
                link.href = url;
                link.target = "_blank";
                link.textContent = fileName;
                link.style.color = "#2563eb";
                link.style.textDecoration = "none";
                
                link.addEventListener('mouseover', () => {
                    link.style.textDecoration = "underline";
                });
                
                link.addEventListener('mouseout', () => {
                    link.style.textDecoration = "none";
                });

                li.appendChild(link);
                
                if (sourceType) {
                    const typeSpan = document.createElement('span');
                    typeSpan.textContent = ` (${sourceType})`;
                    typeSpan.style.color = "#9ca3af";
                    li.appendChild(typeSpan);
                }
            } else {
                const span = document.createElement('span');
                span.textContent = fileName;
                span.style.color = "#374151";
                li.appendChild(span);
                
                if (sourceType) {
                    const typeSpan = document.createElement('span');
                    typeSpan.textContent = ` (${sourceType})`;
                    typeSpan.style.color = "#9ca3af";
                    li.appendChild(typeSpan);
                }
            }

            // Add relevance score if available
            if (relevanceScore !== null) {
                const relevanceSpan = document.createElement('span');
                relevanceSpan.textContent = ` - ${relevanceScore}% match`;
                relevanceSpan.style.color = "#9ca3af";
                relevanceSpan.style.fontSize = "9px";
                li.appendChild(relevanceSpan);
            }

            ul.appendChild(li);
        });

        srcContainer.appendChild(srcTitle);
        srcContainer.appendChild(ul);
        bubble.appendChild(srcContainer);
    }

    /* ---------- TIME ---------- */

    const time = document.createElement('div');
    time.className = 'sb-time';
    time.textContent = new Date().toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    });

    bubble.appendChild(time);

    row.appendChild(bubble);

    messagesEl.appendChild(row);

    scrollToBottom();
}

    // Typing Indicator
    function showTyping() {
        const id = 'typing-' + Date.now();
        const row = document.createElement('div');
        row.className = 'sb-msg-row bot';
        row.id = id;

        const avatar = document.createElement('div');
        avatar.className = 'sb-avatar bot';
        avatar.textContent = botIcon;
        row.appendChild(avatar);

        const bubble = document.createElement('div');
        bubble.className = 'sb-typing';
        bubble.innerHTML = '<div class="sb-dot"></div><div class="sb-dot"></div><div class="sb-dot"></div>';
        
        row.appendChild(bubble);
        messagesEl.appendChild(row);
        scrollToBottom();
        return id;
    }

    function removeTyping(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    // Start
    init();

})();
// 댕댕이 월드 - 챗봇 위젯
// 서버(/chatbot/chat)가 SSE 로 답변을 조금씩 내려주면 fetch 스트림으로 읽어서 말풍선에 이어 붙인다.
// layout.html 에서 모든 페이지에 include 되기 때문에 전역 이름이 app.js, roulette 쪽이랑 겹치지 않게
// 변수/함수 앞에 chatbot 을 붙였다. (history 같은 이름은 window.history 를 덮어쓰니까 특히 주의)

// Constants
const CHATBOT_STORAGE_KEY = 'dd-chatbot-history';
const CHATBOT_KEEP_LIMIT = 40;   // 브라우저에 저장해 두는 최대 메시지 수
const CHATBOT_SEND_LIMIT = 12;   // 서버로 보내는 최근 메시지 수 (서버 chatbot.max-history 와 맞춰 둠)

// State
let chatbotHistory = [];   // [{ role: 'user' | 'assistant', content: '...' }]
let chatbotBusy = false;   // 답변 받는 중이면 true → 중복 전송 막음

// DOM Elements
const chatbotOpenButton = document.getElementById('chatbotOpenButton');
const chatbotPanel = document.getElementById('chatbotPanel');
const chatbotMessageList = document.getElementById('chatbotMessageList');
const chatbotForm = document.getElementById('chatbotForm');
const chatbotInput = document.getElementById('chatbotInput');
const chatbotSendButton = document.getElementById('chatbotSendButton');
const chatbotCloseButton = document.getElementById('chatbotCloseButton');
const chatbotResetButton = document.getElementById('chatbotResetButton');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    // fragment 가 없는 페이지(로그인 화면 등)에서는 아무것도 안 한다
    if (!chatbotOpenButton || !chatbotPanel) return;

    loadChatbotHistory();
    renderChatbotMessages();
    initChatbotEventListeners();
    checkChatbotEnabled();
});

// Event Listeners
function initChatbotEventListeners() {
    // 열기 / 닫기
    chatbotOpenButton.addEventListener('click', () => {
        openChatbot(true);
    });

    chatbotCloseButton.addEventListener('click', () => {
        openChatbot(false);
    });

    // 대화 지우기
    chatbotResetButton.addEventListener('click', () => {
        chatbotHistory = [];
        saveChatbotHistory();
        renderChatbotMessages();
    });

    // 전송 버튼 / 폼 submit
    chatbotForm.addEventListener('submit', handleChatbotSubmit);

    // Enter 는 전송, Shift+Enter 는 줄바꿈
    // isComposing 을 안 보면 한글 조합 중에 Enter 쳤을 때 마지막 글자가 한 번 더 전송된다
    chatbotInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
            e.preventDefault();
            chatbotForm.requestSubmit();
        }
    });

    // 입력한 줄 수만큼 textarea 높이 늘리기 (최대 높이는 css 에서 제한)
    chatbotInput.addEventListener('input', () => {
        chatbotInput.style.height = 'auto';
        chatbotInput.style.height = chatbotInput.scrollHeight + 'px';
    });
}

// Check Enabled
// 서버에서 chatbot.enabled=false 로 꺼 두면 버튼이 안 보이게 한다.
// (버튼은 html 에 hidden 으로 들어 있고, 켜져 있다고 확인된 뒤에만 보여 준다)
function checkChatbotEnabled() {
    fetch('/chatbot/config')
        .then(res => res.json())
        .then(config => {
            if (config.enabled) {
                chatbotOpenButton.hidden = false;
            }
        })
        .catch(() => {
            // config 를 못 받으면 그냥 버튼을 안 보여 준다. 화면에 에러까지 띄울 일은 아님
        });
}

// Open / Close
function openChatbot(isOpen) {
    chatbotPanel.hidden = !isOpen;
    chatbotOpenButton.hidden = isOpen;

    if (isOpen) {
        chatbotMessageList.scrollTop = chatbotMessageList.scrollHeight;
        chatbotInput.focus();
    }
}

// Load / Save History
// 대화 이력은 서버에 두지 않고 sessionStorage 에만 둔다. 탭을 닫으면 사라짐.
function loadChatbotHistory() {
    try {
        chatbotHistory = JSON.parse(sessionStorage.getItem(CHATBOT_STORAGE_KEY) || '[]');
    } catch (e) {
        // 값이 깨져 있으면 새로 시작
        chatbotHistory = [];
    }
}

function saveChatbotHistory() {
    sessionStorage.setItem(CHATBOT_STORAGE_KEY, JSON.stringify(chatbotHistory.slice(-CHATBOT_KEEP_LIMIT)));
}

// Render Messages
function renderChatbotMessages() {
    chatbotMessageList.textContent = '';
    addChatbotMessage('assistant', '안녕하세요! 댕댕이 월드 도우미예요 🐶 룰렛 이벤트, 앱 다운로드, 반려견 상식 무엇이든 물어보세요.');
    chatbotHistory.forEach(m => addChatbotMessage(m.role, m.content));
}

// Add Message
// 말풍선 하나를 만들어서 맨 아래에 붙이고, 나중에 내용을 계속 바꿀 수 있게 요소를 돌려준다.
// innerHTML 이 아니라 textContent 로 넣는 이유: 답변에 태그가 섞여 와도 그냥 글자로만 보이게 하려고 (XSS 방지)
function addChatbotMessage(role, text) {
    const message = document.createElement('div');
    message.className = 'cb-msg ' + role;
    message.textContent = text;
    chatbotMessageList.appendChild(message);
    chatbotMessageList.scrollTop = chatbotMessageList.scrollHeight;
    return message;
}

// Handle Submit
function handleChatbotSubmit(e) {
    e.preventDefault();

    const text = chatbotInput.value.trim();
    if (!text || chatbotBusy) return;

    chatbotInput.value = '';
    chatbotInput.style.height = 'auto';
    sendChatbotMessage(text);
}

// Send Message
// 질문을 보내고, 돌아오는 SSE 를 읽으면서 답변 말풍선을 채운다.
async function sendChatbotMessage(text) {
    chatbotBusy = true;
    chatbotSendButton.disabled = true;

    chatbotHistory.push({ role: 'user', content: text });
    addChatbotMessage('user', text);

    // 빈 말풍선을 먼저 만들어 두고 delta 가 올 때마다 내용을 늘린다
    const answerBox = addChatbotMessage('assistant', '');
    answerBox.classList.add('typing');
    let answer = '';
    let failed = false;

    try {
        const res = await fetch('/chatbot/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
            body: JSON.stringify({ messages: chatbotHistory.slice(-CHATBOT_SEND_LIMIT) })
        });
        if (!res.body) throw new Error('no stream');

        const reader = res.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';

        // SSE 는 빈 줄('\n\n')로 이벤트가 구분된다. 청크가 이벤트 중간에서 끊겨 올 수 있어서
        // 버퍼에 쌓아 두고 완성된 블록만 꺼내 쓴다.
        for (;;) {
            const { value, done } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });

            let end;
            while ((end = buffer.indexOf('\n\n')) >= 0) {
                const block = buffer.slice(0, end);
                buffer = buffer.slice(end + 2);

                const parsed = parseChatbotEvent(block);
                if (!parsed) continue;

                if (parsed.event === 'error') {
                    failed = true;
                    answerBox.className = 'cb-msg error';
                    answerBox.textContent = parsed.payload.message || '오류가 발생했어요.';
                } else if (parsed.payload.delta) {
                    answer += parsed.payload.delta;
                    answerBox.textContent = answer;
                    chatbotMessageList.scrollTop = chatbotMessageList.scrollHeight;
                }
            }
        }

        if (failed) {
            // 실패한 질문은 이력에서 빼 둔다. 그래야 같은 질문을 다시 보내도 중복이 안 쌓임
            chatbotHistory.pop();
        } else if (!answer) {
            answerBox.className = 'cb-msg error';
            answerBox.textContent = '답변을 받지 못했어요.';
            chatbotHistory.pop();
        } else {
            chatbotHistory.push({ role: 'assistant', content: answer });
        }
    } catch (e) {
        answerBox.className = 'cb-msg error';
        answerBox.textContent = '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.';
        chatbotHistory.pop();
    } finally {
        answerBox.classList.remove('typing');
        chatbotBusy = false;
        chatbotSendButton.disabled = false;
        saveChatbotHistory();
        chatbotInput.focus();
    }
}

// Parse Event
// SSE 블록 하나("event: xxx\ndata: {...}")를 { event, payload } 로 바꾼다.
// [DONE] 이나 JSON 이 아닌 조각(keep-alive 등)은 쓸 게 없으니 null
function parseChatbotEvent(block) {
    let event = 'message';
    let data = '';

    block.split('\n').forEach(line => {
        if (line.startsWith('event:')) {
            event = line.slice(6).trim();
        } else if (line.startsWith('data:')) {
            data += line.slice(5).replace(/^ /, '');
        }
    });

    if (!data || data === '[DONE]') return null;

    try {
        return { event: event, payload: JSON.parse(data) };
    } catch (e) {
        return null;
    }
}

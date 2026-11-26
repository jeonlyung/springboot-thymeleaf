/* 챗봇 위젯 — POST /chatbot/chat (SSE) 를 fetch 스트림으로 읽는다. 답변은 textContent 로만 렌더링(XSS 방지). */
(function () {
  'use strict';
  const KEY = 'dd-chatbot-history';
  const MAX_KEEP = 40;
  const $ = (id) => document.getElementById(id);
  const toggle = $('cb-toggle'), panel = $('cb-panel'), log = $('cb-log'),
        form = $('cb-form'), input = $('cb-input'), send = $('cb-send');
  if (!toggle || !panel) return;

  let history = [];
  let busy = false;

  try { history = JSON.parse(sessionStorage.getItem(KEY) || '[]'); } catch (_) { history = []; }
  const save = () => sessionStorage.setItem(KEY, JSON.stringify(history.slice(-MAX_KEEP)));

  function bubble(role, text) {
    const el = document.createElement('div');
    el.className = 'cb-msg ' + role;
    el.textContent = text;
    log.appendChild(el);
    log.scrollTop = log.scrollHeight;
    return el;
  }

  function greet() {
    bubble('assistant', '안녕하세요! 댕댕이 월드 도우미예요 🐶 룰렛 이벤트, 앱 다운로드, 반려견 상식 무엇이든 물어보세요.');
  }

  function render() {
    log.textContent = '';
    greet();
    history.forEach((m) => bubble(m.role, m.content));
  }

  async function ask(text) {
    busy = true; send.disabled = true;
    history.push({ role: 'user', content: text });
    bubble('user', text);
    const out = bubble('assistant', '');
    out.classList.add('typing');
    let answer = '';

    try {
      const res = await fetch('/chatbot/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
        body: JSON.stringify({ messages: history.slice(-12) }),
      });
      if (!res.body) throw new Error('no stream');

      const reader = res.body.getReader();
      const dec = new TextDecoder('utf-8');
      let buf = '';
      let failed = false;
      for (;;) {
        const { value, done } = await reader.read();
        if (done) break;
        buf += dec.decode(value, { stream: true });
        let idx;
        while ((idx = buf.indexOf('\n\n')) >= 0) {
          const block = buf.slice(0, idx); buf = buf.slice(idx + 2);
          let event = 'message', data = '';
          block.split('\n').forEach((line) => {
            if (line.startsWith('event:')) event = line.slice(6).trim();
            else if (line.startsWith('data:')) data += line.slice(5).replace(/^ /, '');
          });
          if (!data || data === '[DONE]') continue;
          let payload; try { payload = JSON.parse(data); } catch (_) { continue; }
          if (event === 'error') {
            failed = true;
            out.className = 'cb-msg error';
            out.textContent = payload.message || '오류가 발생했어요.';
          } else if (payload.delta) {
            answer += payload.delta;
            out.textContent = answer;
            log.scrollTop = log.scrollHeight;
          }
        }
      }
      if (!failed) {
        if (!answer) { out.className = 'cb-msg error'; out.textContent = '답변을 받지 못했어요.'; history.pop(); }
        else history.push({ role: 'assistant', content: answer });
      } else {
        history.pop();   // 실패한 질문은 이력에서 제거 → 재시도 가능
      }
    } catch (e) {
      out.className = 'cb-msg error';
      out.textContent = '서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.';
      history.pop();
    } finally {
      out.classList.remove('typing');
      busy = false; send.disabled = false; save(); input.focus();
    }
  }

  function open(v) {
    panel.hidden = !v; toggle.hidden = v;
    if (v) { log.scrollTop = log.scrollHeight; input.focus(); }
  }

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const text = input.value.trim();
    if (!text || busy) return;
    input.value = ''; input.style.height = 'auto';
    ask(text);
  });
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); form.requestSubmit(); }
  });
  input.addEventListener('input', () => { input.style.height = 'auto'; input.style.height = input.scrollHeight + 'px'; });
  toggle.addEventListener('click', () => open(true));
  $('cb-close').addEventListener('click', () => open(false));
  $('cb-reset').addEventListener('click', () => { history = []; save(); render(); });

  render();
  // 서버에서 비활성화(chatbot.enabled=false)면 버튼 자체를 숨긴다.
  fetch('/chatbot/config').then((r) => r.json()).then((c) => { if (c.enabled) toggle.hidden = false; })
    .catch(() => {});
})();

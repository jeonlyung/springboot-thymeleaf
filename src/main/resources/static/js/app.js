// 멍마켓 - 강아지 커뮤니티 App

// Sample Data
const samplePosts = [
    {
        id: 1,
        title: "3개월 된 골든리트리버 분양합니다",
        category: "adoption",
        categoryLabel: "분양",
        price: "무료분양",
        location: "역삼동",
        time: "3분 전",
        image: "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=400&fit=crop",
        likes: 12,
        chats: 5,
        views: 234,
        userName: "댕댕이맘",
        userAvatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
        description: "3개월 된 건강한 골든리트리버 분양합니다.\n\n예방접종 완료, 건강검진 완료했습니다.\n사정이 생겨 좋은 가정에 분양하고자 합니다.\n\n직접 만나서 상담 후 분양 결정하겠습니다.\n사료, 용품 함께 드려요!"
    },
    {
        id: 2,
        title: "주말 아침 산책 같이 하실 분~",
        category: "walk",
        categoryLabel: "산책 메이트",
        price: null,
        location: "역삼동",
        time: "15분 전",
        image: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=400&fit=crop",
        likes: 8,
        chats: 3,
        views: 156,
        userName: "뽀삐아빠",
        userAvatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
        description: "매주 토/일 아침 7시에 역삼공원에서 산책해요!\n\n저희 뽀삐는 2살 포메라니안이고 친화력 좋습니다.\n소형견 키우시는 분들 같이 산책해요~\n\n관심있으시면 채팅주세요!"
    },
    {
        id: 3,
        title: "강아지 캐리어 나눔합니다 (중형견용)",
        category: "supplies",
        categoryLabel: "용품 나눔",
        price: "나눔",
        location: "삼성동",
        time: "1시간 전",
        image: "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=400&h=400&fit=crop",
        likes: 24,
        chats: 12,
        views: 432,
        userName: "펫러버",
        userAvatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
        description: "중형견용 이동 캐리어 나눔해요.\n\n크기: 50x35x35cm\n상태: 사용감 있지만 깨끗해요\n\n직거래만 가능합니다.\n역삼역 근처에서 만나요~"
    },
    {
        id: 4,
        title: "실종) 검은색 푸들 찾습니다ㅠㅠ",
        category: "lost",
        categoryLabel: "실종/발견",
        price: "사례금 50만원",
        location: "청담동",
        time: "2시간 전",
        image: "https://images.unsplash.com/photo-1575425186775-b8de9a427e67?w=400&h=400&fit=crop",
        likes: 56,
        chats: 8,
        views: 1205,
        userName: "검둥이주인",
        userAvatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop",
        description: "1월 30일 오후 3시경 청담동 갤러리아백화점 근처에서 실종되었습니다.\n\n이름: 검둥이\n종: 토이푸들\n나이: 4살\n특징: 검은색, 빨간 목줄\n\n발견하시면 연락 부탁드립니다.\n사례금 드립니다ㅠㅠ"
    },
    {
        id: 5,
        title: "웰시코기 분양받으실 분 (유료)",
        category: "adoption",
        categoryLabel: "분양",
        price: "500,000원",
        location: "역삼동",
        time: "3시간 전",
        image: "https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=400&h=400&fit=crop",
        likes: 45,
        chats: 15,
        views: 892,
        userName: "코기러버",
        userAvatar: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&h=100&fit=crop",
        description: "직접 번식한 건강한 웰시코기 분양합니다.\n\n생년월일: 2025년 12월 15일\n성별: 수컷 2마리, 암컷 1마리\n\n기본 예방접종 완료\n분양 후 한달간 건강 보장\n\n책임감 있는 보호자에게만 분양합니다."
    },
    {
        id: 6,
        title: "강아지 유모차 팝니다",
        category: "supplies",
        categoryLabel: "용품 나눔",
        price: "80,000원",
        location: "역삼동",
        time: "5시간 전",
        image: "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=400&h=400&fit=crop",
        likes: 15,
        chats: 4,
        views: 267,
        userName: "멍멍이집사",
        userAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
        description: "거의 새것 강아지 유모차 판매합니다.\n\n구매가: 15만원\n사용기간: 2개월\n\n저희 강아지가 유모차를 싫어해서 거의 못썼어요ㅠ\n상태 좋습니다!"
    }
];

// State
let currentCategory = 'all';
let posts = [...samplePosts];
let selectedPost = null;

// DOM Elements
const mainContent = document.getElementById('mainContent');
const categoryChips = document.querySelectorAll('.category-chip');
const navItems = document.querySelectorAll('.nav-item');
const fabBtn = document.getElementById('fabBtn');
const locationBtn = document.getElementById('locationBtn');

// Modals
const locationModal = document.getElementById('locationModal');
const closeLocationModal = document.getElementById('closeLocationModal');
const postDetailModal = document.getElementById('postDetailModal');
const closePostDetail = document.getElementById('closePostDetail');
const writeModal = document.getElementById('writeModal');
const closeWriteModal = document.getElementById('closeWriteModal');
const likeBtn = document.getElementById('likeBtn');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    renderPosts();
    initEventListeners();
});

// Event Listeners
function initEventListeners() {
    // Category Filter
    categoryChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const category = chip.dataset.category;
            setActiveCategory(category);
            filterPosts(category);
        });
    });

    // Bottom Navigation
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            setActiveNav(item);
        });
    });

    // FAB Button
    fabBtn.addEventListener('click', () => {
        openModal(writeModal);
    });

    // Location Button
    locationBtn.addEventListener('click', () => {
        openModal(locationModal);
    });

    // Close Modals
    closeLocationModal.addEventListener('click', () => {
        closeModal(locationModal);
    });

    closePostDetail.addEventListener('click', () => {
        closeModal(postDetailModal);
    });

    closeWriteModal.addEventListener('click', () => {
        closeModal(writeModal);
    });

    // Like Button
    likeBtn.addEventListener('click', () => {
        likeBtn.classList.toggle('active');
    });

    // Price Toggle
    const priceToggle = document.getElementById('priceToggle');
    const priceInput = document.getElementById('priceInput');
    priceToggle.addEventListener('change', () => {
        priceInput.disabled = !priceToggle.checked;
        if (!priceToggle.checked) {
            priceInput.value = '';
        }
    });

    // Submit Post
    const submitPost = document.getElementById('submitPost');
    submitPost.addEventListener('click', handleSubmitPost);

    // Location Items
    const locationItems = document.querySelectorAll('.location-item');
    locationItems.forEach(item => {
        item.addEventListener('click', () => {
            locationItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const locationText = document.querySelector('.location-text');
            locationText.textContent = item.querySelector('span').textContent;
            closeModal(locationModal);
        });
    });

    // Close modal on overlay click
    [locationModal, postDetailModal, writeModal].forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal(modal);
            }
        });
    });

    // Handle back button
    window.addEventListener('popstate', (e) => {
        if (postDetailModal.classList.contains('active')) {
            closeModal(postDetailModal);
        } else if (writeModal.classList.contains('active')) {
            closeModal(writeModal);
        }
    });
}

// Render Posts
function renderPosts() {
    const filteredPosts = currentCategory === 'all'
        ? posts
        : posts.filter(post => post.category === currentCategory);

    if (filteredPosts.length === 0) {
        mainContent.innerHTML = `
            <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M9.5 2A2.5 2.5 0 0 1 12 4.5v15a2.5 2.5 0 0 1-4.96.44 2.5 2.5 0 0 1-2.96-3.08 3 3 0 0 1-.34-5.58 2.5 2.5 0 0 1 1.32-4.24 2.5 2.5 0 0 1 1.98-3A2.5 2.5 0 0 1 9.5 2Z"/>
                    <path d="M14.5 2A2.5 2.5 0 0 0 12 4.5v15a2.5 2.5 0 0 0 4.96.44 2.5 2.5 0 0 0 2.96-3.08 3 3 0 0 0 .34-5.58 2.5 2.5 0 0 0-1.32-4.24 2.5 2.5 0 0 0-1.98-3A2.5 2.5 0 0 0 14.5 2Z"/>
                </svg>
                <h3>아직 게시글이 없어요</h3>
                <p>우리 동네 첫 번째 게시글을 작성해보세요!</p>
            </div>
        `;
        return;
    }

    mainContent.innerHTML = filteredPosts.map(post => createPostCard(post)).join('');

    // Add click listeners to post cards
    document.querySelectorAll('.post-card').forEach(card => {
        card.addEventListener('click', () => {
            const postId = parseInt(card.dataset.id);
            openPostDetail(postId);
        });
    });
}

// Create Post Card HTML
function createPostCard(post) {
    return `
        <article class="post-card" data-id="${post.id}">
            <div class="post-image">
                <img src="${post.image}" alt="${post.title}" loading="lazy">
            </div>
            <div class="post-info">
                <span class="category-badge ${post.category}">${post.categoryLabel}</span>
                <h3 class="post-title">${post.title}</h3>
                <div class="post-meta">
                    <span>${post.location}</span>
                    <span>${post.time}</span>
                </div>
                ${post.price ? `<div class="post-price">${post.price}</div>` : ''}
                <div class="post-stats">
                    <span class="stat-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                        ${post.likes}
                    </span>
                    <span class="stat-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                        ${post.chats}
                    </span>
                </div>
            </div>
        </article>
    `;
}

// Set Active Category
function setActiveCategory(category) {
    currentCategory = category;
    categoryChips.forEach(chip => {
        chip.classList.toggle('active', chip.dataset.category === category);
    });
}

// Filter Posts
function filterPosts(category) {
    renderPosts();
}

// Set Active Nav
function setActiveNav(activeItem) {
    navItems.forEach(item => {
        item.classList.toggle('active', item === activeItem);
    });
}

// Open Modal
function openModal(modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    history.pushState({ modal: true }, '');
}

// Close Modal
function closeModal(modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
}

// Open Post Detail
function openPostDetail(postId) {
    selectedPost = posts.find(p => p.id === postId);
    if (!selectedPost) return;

    const detailBody = document.getElementById('postDetailBody');
    const detailPrice = document.getElementById('detailPrice');

    detailBody.innerHTML = `
        <div class="detail-image-slider">
            <img src="${selectedPost.image}" alt="${selectedPost.title}">
            <span class="image-counter">1/1</span>
        </div>
        <div class="detail-user-info">
            <div class="user-avatar">
                <img src="${selectedPost.userAvatar}" alt="${selectedPost.userName}">
            </div>
            <div class="user-info-text">
                <div class="user-name">${selectedPost.userName}</div>
                <div class="user-location">${selectedPost.location}</div>
            </div>
        </div>
        <div class="detail-content">
            <span class="category-badge ${selectedPost.category} detail-category">${selectedPost.categoryLabel}</span>
            <h1 class="detail-title">${selectedPost.title}</h1>
            <p class="detail-meta">${selectedPost.time} · 조회 ${selectedPost.views}</p>
            <p class="detail-description">${selectedPost.description}</p>
        </div>
        <div class="detail-stats">
            <span>관심 ${selectedPost.likes}</span>
            <span>채팅 ${selectedPost.chats}</span>
            <span>조회 ${selectedPost.views}</span>
        </div>
    `;

    detailPrice.textContent = selectedPost.price || '가격 정보 없음';

    openModal(postDetailModal);
}

// Handle Submit Post
function handleSubmitPost() {
    const title = document.getElementById('postTitle').value.trim();
    const content = document.getElementById('postContent').value.trim();
    const category = document.getElementById('postCategory').value;
    const priceToggle = document.getElementById('priceToggle').checked;
    const price = document.getElementById('priceInput').value.trim();

    if (!title || !content || !category) {
        alert('제목, 내용, 카테고리를 모두 입력해주세요.');
        return;
    }

    const categoryLabels = {
        'adoption': '분양',
        'walk': '산책 메이트',
        'lost': '실종/발견',
        'supplies': '용품 나눔'
    };

    const newPost = {
        id: posts.length + 1,
        title: title,
        category: category,
        categoryLabel: categoryLabels[category],
        price: priceToggle && price ? price : null,
        location: document.querySelector('.location-text').textContent,
        time: '방금 전',
        image: 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=400&fit=crop',
        likes: 0,
        chats: 0,
        views: 0,
        userName: '나',
        userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
        description: content
    };

    posts.unshift(newPost);
    renderPosts();

    // Reset form
    document.getElementById('postTitle').value = '';
    document.getElementById('postContent').value = '';
    document.getElementById('postCategory').value = '';
    document.getElementById('priceToggle').checked = false;
    document.getElementById('priceInput').value = '';
    document.getElementById('priceInput').disabled = true;

    closeModal(writeModal);
}

// Utility: Format Number
function formatNumber(num) {
    if (num >= 10000) {
        return (num / 10000).toFixed(1) + '만';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + '천';
    }
    return num.toString();
}

// Pull to Refresh (optional enhancement)
let touchStartY = 0;
let touchEndY = 0;

mainContent.addEventListener('touchstart', (e) => {
    touchStartY = e.touches[0].clientY;
}, { passive: true });

mainContent.addEventListener('touchend', (e) => {
    touchEndY = e.changedTouches[0].clientY;
    if (touchEndY - touchStartY > 100 && window.scrollY === 0) {
        // Trigger refresh
        renderPosts();
    }
}, { passive: true });

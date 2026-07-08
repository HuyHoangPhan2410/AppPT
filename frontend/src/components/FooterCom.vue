<template>
  <footer class="pt-footer">
    <div class="footer-content">
      <h3 class="footer-title">Personal Trainer Manager - Đào Tạo & Huấn Luyện Thể Hình</h3>
      <p class="footer-text">📍 Địa chỉ: Hệ thống Ways Station Gym</p>
      <p class="footer-text">📞 SĐT: 098.xxx.xxxx (Liên hệ tư vấn lịch tập)</p>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.pt-footer {
  background-color: #b30404;
  color: #ffffff;
  padding: 25px 20px;
  text-align: center;
  margin-top: auto; /* Push to bottom if flex container */
  border-top: 3px solid #7a0000;
}
.footer-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.footer-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.footer-text {
  margin: 0;
  font-size: 0.95rem;
  color: #ffe6e6;
}

/* Mobile responsive */
@media (max-width: 600px) {
  .pt-footer {
    padding: 20px 15px;
  }
  .footer-title {
    font-size: 1rem;
  }
  .footer-text {
    font-size: 0.85rem;
  }
}
</style>

import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { startCapturingKeys } from './utils/keyboardInput';

import "@/assets/base.scss";
import "@/assets/components.scss";
// import "@/assets/screenshot.scss";

const app = createApp(App);

startCapturingKeys();

app.use(router);

app.mount('#app');

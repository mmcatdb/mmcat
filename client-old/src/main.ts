import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { startCapturingKeys } from './utils/keyboardInput';

import '@/assets/components.scss';

const app = createApp(App);

startCapturingKeys();

app.use(router);

app.mount('#app');

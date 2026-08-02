import axios from "axios";

const AUTH_TOKEN_KEY = 'auth-token';

const httpClient = axios.create({
    baseURL: process.env.VUE_APP_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
    withCredentials: true
});

const getAuthToken = () => localStorage.getItem(AUTH_TOKEN_KEY);

const authInterceptor = (config: any) => {
    const token = getAuthToken();
    if (token) {
        config.headers[AUTH_TOKEN_KEY] = token;
    }
    return config;
}

export function setupHttpInterceptors(store: any, router: any) {
    httpClient.interceptors.request.use(authInterceptor);

    httpClient.interceptors.response.use(r => r, function (error) {
        if (error.response.status === 401 && error.response.config.url !== '/logout') {
            store.dispatch('logout')
                .then(() => {
                    router.push({
                        name: 'Login'
                    });
                })

            console.error('Сервер вернул 401, авторизация на фронтенде принудительно удалена');
        }
        return Promise.reject(error);
    });
}

export default httpClient;

export {
    AUTH_TOKEN_KEY
};
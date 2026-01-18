import http from 'k6/http';
import { check, sleep } from 'k6';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://main-api:8080';

const bigImageFile = new Uint8Array(5 * 1024 * 1024); // 5MB

const USER_CREDENTIALS = {
    email: 'admintest@test.pl',
    password: '123',
    name: 'admin',
};

export function runUploadTest() {

    const registerRes = http.post(
        `${BASE_URL}/auth/signup`,
        JSON.stringify(USER_CREDENTIALS),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'RegisterRequest' }
        }
    );

    if (registerRes.status !== 200 && registerRes.status !== 409) {
        console.error(`Błąd Rejestracji: ${registerRes.status} ${registerRes.body}`);
    }

    // 2. LOGOWANIE (endpoint: /auth/login)
    const loginRes = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({
            email: USER_CREDENTIALS.email,
            password: USER_CREDENTIALS.password
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'LoginRequest' }
        }
    );

    if (loginRes.status !== 200) {
        console.error(`Błąd Logowania! Status: ${loginRes.status}`);
        return;
    }

    const fd = new FormData();
    fd.append('title', `Test ${randomString(5)}`);
    fd.append('description', 'Opis testowy');
    fd.append('price', '123');
    fd.append('categoryId', '1');
    fd.append('location', 'Warszawa');
    fd.append('image', http.file(bigImageFile, 'test.jpg', 'image/jpeg'));

    const uploadRes = http.post(`${BASE_URL}/advertisements/new`, fd.body(), {
        headers: {
            'Content-Type': 'multipart/form-data; boundary=' + fd.boundary,
        },
        tags: { name: 'UploadRequest' }
    });

    check(uploadRes, {
        'Status 201': (r) => r.status === 201,
    });
}
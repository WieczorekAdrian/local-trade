import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { randomString, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://main-api:8080';

const bigFileShared = new SharedArray('largeFile', function () {
    return [{
        content: 'x'.repeat(5 * 1024 * 1024)
    }];
});

const USER_CREDENTIALS = {
    email: 'admintest@test.pl',
    password: '123'
};

export function runUploadTest() {

    const loginRes = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify(USER_CREDENTIALS),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'LoginRequest' }
        }
    );

    if (loginRes.status !== 200) {
        console.error(` Błąd Logowania! Status: ${loginRes.status}`);
        return;
    }

    const fd = new FormData();

    const dtoPayload = {
        title: `Stress Test ${randomString(5)}`,
        description: 'Automatyczny test k6',
        price: randomIntBetween(100, 10000),
        categoryId: 1,
        location: 'Warszawa',
        active: true,
        image: null
    };

    fd.append(
        'advertisementDto',
        http.file(JSON.stringify(dtoPayload), 'ad.json', 'application/json')
    );

    fd.append(
        'files',
        http.file(bigFileShared[0].content, 'test.jpg', 'image/jpeg')
    );

    const uploadRes = http.post(`${BASE_URL}/advertisements/new`, fd.body(), {
        headers: {
            'Content-Type': 'multipart/form-data; boundary=' + fd.boundary,
        },
        tags: { name: 'UploadRequest' }
    });

    if (uploadRes.status !== 200) {
        console.error(`UPLOAD FAILED! Status: ${uploadRes.status}`);
        try { console.error(` Details: ${uploadRes.body}`); } catch(e) {}
    }

    check(uploadRes, {
        'Status 200 ': (r) => r.status === 200,
    });

    sleep(1);
}
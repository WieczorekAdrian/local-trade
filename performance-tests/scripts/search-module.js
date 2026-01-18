import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

const BASE_URL = 'http://main-api:8080';

export function runSearchTest() {
    let res = http.get(`${BASE_URL}/advertisements/search`);

    check(res, {
        'listing status 200': (r) => r.status === 200,
        'listing fast (<500ms)': (r) => r.timings.duration < 500,
    });
    sleep(1);

    const maxPrice = randomIntBetween(100, 1000);

    const params = {
        tags: { name: 'SearchWithFilters' },
    };

    res = http.get(`${BASE_URL}/advertisements/search?maxPrice=${maxPrice}&sort=price,asc`, params);

    check(res, {
        'listing status 200': (r) => r.status === 200,
    });
    sleep(2);
}
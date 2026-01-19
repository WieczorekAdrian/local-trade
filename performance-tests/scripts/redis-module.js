import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = 'http://main-api:8080';

export const options = {
    vus: 1,
    iterations: 1,
};

export default function () {
    const resCold = http.get(`${BASE_URL}/categories`, {
        tags: { my_tag: 'cold_start' }
    });

    check(resCold, {
        'Cold status is 200': (r) => r.status === 200,
    });

    sleep(1);

    for (let i = 0; i < 20; i++) {
        const resWarm = http.get(`${BASE_URL}/categories`, {
            tags: { my_tag: 'warm_cache' }
        });

        check(resWarm, {
            'Warm status is 200': (r) => r.status === 200,
        });
        console.log(`❄ Warm Request Time: ${resWarm.timings.duration} ms`);
    }
    console.log(`❄ Cold Request Time: ${resCold.timings.duration} ms`);
}
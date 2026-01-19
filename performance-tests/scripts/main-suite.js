import { runSearchTest } from './search-module.js';
import { runUploadTest } from './upload-module.js';

export const options = {
    scenarios: {
        search_traffic: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 10 },
                { duration: '20s', target: 10 },
                { duration: '10s', target: 0 },
            ],
            gracefulStop: '5s',
            exec: 'search',
        },

        upload_heavy: {
            executor: 'constant-vus',
            vus: 5,
            duration: '20s',
            startTime: '15s',
            exec: 'upload',
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<2000'],
    },
};

export function search() {
    runSearchTest();
}

export function upload() {
    runUploadTest();
}
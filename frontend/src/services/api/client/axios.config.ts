import axios from 'axios'
import { ENV } from '@config/env.config'


const apiClient = axios.create({
    baseURL: ENV.API_BASE_URL,
    timeout: 30_000, // 30 seconds
    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
    },
})

export default apiClient

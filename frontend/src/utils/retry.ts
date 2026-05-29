// Simple retry helper with exponential backoff. Non-intrusive and usable for UI fetches.
export async function retry<T>(fn: () => Promise<T>, attempts = 3, baseDelayMs = 300): Promise<T> {
    let lastError: any
    for (let i = 0; i < attempts; i++) {
        try {
            return await fn()
        } catch (err) {
            lastError = err
            const delay = baseDelayMs * Math.pow(2, i)
            // wait before retrying (simple sleep)
            await new Promise((res) => setTimeout(res, delay))
        }
    }
    throw lastError
}


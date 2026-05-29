export const compactParams = <T extends Record<string, unknown>>(params: T): Partial<T> => {
    return Object.fromEntries(
        Object.entries(params).filter(([, value]) => {
            return value !== undefined && value !== null && value !== ''
        }),
    ) as Partial<T>
}


export const classNames = (...parts: Array<string | false | null | undefined>) => {
    return parts.filter(Boolean).join(' ')
}


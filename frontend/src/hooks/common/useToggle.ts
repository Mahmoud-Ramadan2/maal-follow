import { useState, useCallback, useMemo } from 'react'

/** Return type of `useToggle`. */
type UseToggleReturn = [
    /** Current boolean value */
    value: boolean,
    /** Flip to the opposite value */
    toggle: () => void,
    /** Force `true` */
    setTrue: () => void,
    /** Force `false` */
    setFalse: () => void,
]

/**
 * Manages a boolean flag with convenience methods.
 *
 * Ideal for modals, drawers, dropdowns, expand/collapse —
 * any UI that toggles between two states.
 *
 * @param initialValue - Starting value (`false` by default).
 * @returns A tuple: `[value, toggle, setTrue, setFalse]`.
 *
 * @example
 * ```tsx
 * const [isOpen, toggleOpen, openModal, closeModal] = useToggle()
 *
 * <button onClick={openModal}>Open</button>
 *
 * <Modal open={isOpen} onClose={closeModal}>
 *     …
 * </Modal>
 * ```
 *
 * @example
 * ```tsx
 * const [showPassword, togglePassword] = useToggle(false)
 *
 * <Input
 *     type={showPassword ? 'text' : 'password'}
 *     endAdornment={
 *         <IconButton onClick={togglePassword}>
 *             {showPassword ? <VisibilityOff /> : <Visibility />}
 *         </IconButton>
 *     }
 * />
 * ```
 */
export function useToggle(initialValue: boolean = false): UseToggleReturn {
    const [value, setValue] = useState(initialValue)

    const toggle   = useCallback(() => setValue((prev) => !prev), [])
    const setTrue  = useCallback(() => setValue(true), [])
    const setFalse = useCallback(() => setValue(false), [])

    return useMemo(
        () => [value, toggle, setTrue, setFalse],
        [value, toggle, setTrue, setFalse],
    )
}


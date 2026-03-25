import { useEffect, useCallback, useRef } from 'react'
import type { ReactNode, MouseEvent } from 'react'
import { createPortal } from 'react-dom'
import './Modal.css'

// ────────────────────────────────────────────────────────────
// Types
// ────────────────────────────────────────────────────────────

interface ModalProps {
    /** Controls visibility */
    isOpen: boolean
    /** Called when the modal should close (backdrop, ESC, or ✕ button) */
    onClose: () => void
    /** Header title text */
    title?: string
    /** Main content */
    children: ReactNode
    /** Optional footer (e.g. action buttons) */
    footer?: ReactNode
}

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────

/**
 * Accessible modal dialog.
 *
 * - Rendered via `createPortal` into `document.body`
 * - Closes on backdrop click, ESC key, or ✕ button
 * - Locks body scroll while open
 * - Focus-traps the dialog (basic: focuses close button on mount)
 *
 * @example
 * ```tsx
 * const [open, , show, hide] = useToggle()
 *
 * <Button onClick={show}>Open</Button>
 *
 * <Modal isOpen={open} onClose={hide} title="Confirm">
 *     <p>Are you sure?</p>
 *     <Modal.Footer>
 *         <Button variant="secondary" onClick={hide}>Cancel</Button>
 *         <Button variant="danger" onClick={handleDelete}>Delete</Button>
 *     </Modal.Footer>
 * </Modal>
 * ```
 */
export default function Modal({ isOpen, onClose, title, children, footer }: ModalProps): ReactNode {
    const closeRef = useRef<HTMLButtonElement>(null)
    const hasAutoFocused = useRef(false)

    // ── ESC key listener ───────────────────────────────────
    const handleKeyDown = useCallback(
        (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose()
        },
        [onClose],
    )

    useEffect(() => {
        if (!isOpen)
            hasAutoFocused.current = false
        return

        // Lock body scroll
        const original = document.body.style.overflow
        document.body.style.overflow = 'hidden'

        // Listen for ESC
        document.addEventListener('keydown', handleKeyDown)

        // Focus close button for accessibility
        if (!hasAutoFocused.current) {
            closeRef.current?.focus()
            hasAutoFocused.current = true
        }
        return () => {
            document.body.style.overflow = original
            document.removeEventListener('keydown', handleKeyDown)
        }
    }, [isOpen, handleKeyDown])

    if (!isOpen) return null

    // ── Backdrop click ─────────────────────────────────────
    const handleBackdropClick = (e: MouseEvent<HTMLDivElement>) => {
        // Only close if the click was directly on the backdrop,
        // not on the dialog itself (event bubbling).
        if (e.target === e.currentTarget) onClose()
    }

    return createPortal(
        <div
            className="modal-backdrop"
            onClick={handleBackdropClick}
            role="dialog"
            aria-modal="true"
            aria-labelledby={title ? 'modal-title' : undefined}
        >
            <div className="modal">
                {/* Header */}
                {title && (
                    <div className="modal__header">
                        <h2 id="modal-title" className="modal__title">{title}</h2>
                        <button
                            ref={closeRef}
                            className="modal__close-btn"
                            onClick={onClose}
                            aria-label="Close"
                        >
                            ×
                        </button>
                    </div>
                )}

                {/* Body */}
                <div className="modal__body">
                    {children}
                </div>

                {/* Footer */}
                {footer && (
                    <div className="modal__footer">
                        {footer}
                    </div>
                )}
            </div>
        </div>,
        document.body,
    )
}


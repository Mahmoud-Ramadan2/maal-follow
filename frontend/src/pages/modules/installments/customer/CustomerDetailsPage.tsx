import { useState, useEffect, useCallback } from 'react'
import type { ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
    useCustomer,
    useCustomerDelete,
    useCustomerContracts,
    useCustomerLinkAccount,
} from '@hooks/modules'
import { usePagination, useDebounce } from '@hooks/common'
import { customerApi } from '@services/api/modules/customer.api'
import type { Customer } from '@/types/modules/customer.types'
import Button from '@components/common/Button'
import Input from '@components/common/Input'
import Card from '@components/ui/Card'
import LoadingSpinner from '@components/ui/LoadingSpinner'
import Modal from '@components/common/Modal'
import { APP_ROUTES, ROUTE_HELPERS } from '@/router/routes.config'
import { formatDateTime, formatCurrency, formatDate } from '@utils/helpers/ index'
import type {
    CustomerRelationshipType,
    CustomerContract,
    CustomerAccountLink,
} from '@/types/modules/customer.types'
import { CustomerRelationshipType as RelTypes } from '@/types/modules/customer.types'
import { useDeleteConfirmation } from '@hooks/common/useDeleteConfirmation.ts'
import './CustomerDetailsPage.css'

// ────────────────────────────────────────────────────────────
// Constants
// ────────────────────────────────────────────────────────────
const CONTRACT_PAGE_SIZES = [5, 10, 20] as const

// ────────────────────────────────────────────────────────────
// Component
// ────────────────────────────────────────────────────────────
export default function CustomerDetailsPage(): ReactNode {
    const { id } = useParams<{ id: string }>()
    const customerId = Number(id)
    const navigate = useNavigate()
    const { t } = useTranslation('customer')
    const { t: tc } = useTranslation('common')

    // ── Core data ──────────────────────────────────────────
    const { customer, linkedAccounts, loading, error, refetch } = useCustomer(customerId)
    const { deleteCustomer, loading: deleteLoading } = useCustomerDelete()


    // ── Paginated contracts ────────────────────────────────
    const {
        page: cPage, size: cSize,
        nextPage: cNext, prevPage: cPrev,
        setSize: cSetSize, goToPage: cGoToPage,
    } = usePagination()
    const {
        contracts: paginatedContracts,
        loading: contractsLoading,
        totalPages: contractsTotalPages,
        totalElements: contractsTotalElements,
    } = useCustomerContracts(customerId, cPage, cSize)

    // ── Link accounts ──────────────────────────────────────
    const { linkAccounts, loading: linkLoading } = useCustomerLinkAccount()
    const [showLinkModal, setShowLinkModal] = useState(false)
    const [linkForm, setLinkForm] = useState({
        nationalIdSearch: '',
        relationshipType: 'FAMILY_MEMBER' as CustomerRelationshipType,
        description: '',
    })
    const [searchResults, setSearchResults] = useState<Customer[]>([])
    const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null)
    const [searching, setSearching] = useState(false)
    const debouncedNationalId = useDebounce(linkForm.nationalIdSearch, 400)

    // ── Search customers by national ID ────────────────────
    useEffect(() => {
        if (!debouncedNationalId || debouncedNationalId.length < 3 || selectedCustomer) {
            if (!debouncedNationalId) setSearchResults([])
            return
        }

        let cancelled = false
        const search = async () => {
            setSearching(true)
            try {
                const page = await customerApi.getAll({
                    search: debouncedNationalId,
                    size: 5,
                    page: 0,
                    status: 'active',
                })
                if (!cancelled) {
                    // Exclude the current customer from results
                    setSearchResults(page.content.filter(c => c.id !== customerId))
                }
            } catch {
                if (!cancelled) setSearchResults([])
            } finally {
                if (!cancelled) setSearching(false)
            }
        }
        search()
        return () => { cancelled = true }
    }, [debouncedNationalId, customerId, selectedCustomer])

    // ── Delete confirmation ────────────────────────────────
    const {
        isModalOpen,
        confirmDelete,
        handleDelete,
        handleCloseModal,
    } = useDeleteConfirmation(
        t('deleted'),
        () => navigate(APP_ROUTES.CUSTOMERS.LIST),
    )

    // ── Link account handler ───────────────────────────────
    const handleLinkAccount = async () => {
        if (!selectedCustomer) return
        const success = await linkAccounts({
            customerId,
            linkedCustomerId: selectedCustomer.id,
            relationshipType: linkForm.relationshipType,
            description: linkForm.description || undefined,
        })
        if (success) {
            setShowLinkModal(false)
            resetLinkForm()
            refetch()
        }
    }

    const resetLinkForm = useCallback(() => {
        setLinkForm({ nationalIdSearch: '', relationshipType: 'FAMILY_MEMBER', description: '' })
        setSelectedCustomer(null)
        setSearchResults([])
    }, [])

    // ── Loading state ──────────────────────────────────────
    if (loading) {
        return <div className="customer-details__center"><LoadingSpinner size="lg" /></div>
    }

    // ── Error state ────────────────────────────────────────
    if (error) {
        return (
            <div className="customer-details__center">
                <p className="customer-details__error-text">{error}</p>
                <Button onClick={() => navigate(APP_ROUTES.CUSTOMERS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    // ── Not found ──────────────────────────────────────────
    if (!customer) {
        return (
            <div className="customer-details__center">
                <p className="customer-details__error-text">{t('details.notFound')}</p>
                <Button onClick={() => navigate(APP_ROUTES.CUSTOMERS.LIST)}>{t('details.backToList')}</Button>
            </div>
        )
    }

    // ── Contract pagination info ───────────────────────────
    const cFrom = contractsTotalElements === 0 ? 0 : cPage * cSize + 1
    const cTo = Math.min((cPage + 1) * cSize, contractsTotalElements)

    // ── Contract stats from embedded contracts ─────────────
    const contractStats = customer.contracts
        ? {
            total: customer.contracts.length,
            active: customer.contracts.filter((c: CustomerContract) => c.status === 'ACTIVE').length,
            late: customer.contracts.filter((c: CustomerContract) => c.status === 'LATE').length,
            completed: customer.contracts.filter((c: CustomerContract) => c.status === 'COMPLETED').length,
            totalRemaining: customer.contracts.reduce(
                (sum: number, c: CustomerContract) => sum + (c.remainingAmount || 0), 0,
            ),
            totalFinancial: customer.contracts.reduce(
                (sum: number, c: CustomerContract) => sum + (c.finalPrice || 0), 0,
            ),
        }
        : { total: 0, active: 0, late: 0, completed: 0, totalRemaining: 0, totalFinancial: 0 }

    // ── Determine which contracts to render ────────────────
    // Use paginated data when available, fallback to embedded contracts
    const contractsToRender = paginatedContracts.length > 0
        ? paginatedContracts
        : customer.contracts ?? []

    return (
        <>
            {/* ────────────────────────────────────────────── */}
            {/* 1. Page Header                                 */}
            {/* ────────────────────────────────────────────── */}
            <div className="customer-details__header">
                <div className="customer-details__header-start">
                    <Button variant="secondary" size="sm" onClick={() => navigate(APP_ROUTES.CUSTOMERS.LIST)}>
                        ← {t('details.backToList')}
                    </Button>
                    <h1 className="customer-details__title">{t('details.title', { id: customer.id })}</h1>
                    <span className={`customer-details__badge customer-details__badge--${customer.active ? 'active' : 'inactive'}`}>
                        <span className="customer-details__badge-dot" />
                        {customer.active ? t('status.active') : t('status.inactive')}
                    </span>
                </div>
                <div className="customer-details__header-actions">
                    <Button variant="secondary" onClick={() => navigate(ROUTE_HELPERS.CUSTOMER_EDIT(customer.id))}>
                        {tc('edit')}
                    </Button>
                    <Button
                        variant="danger"
                        onClick={() => confirmDelete(customerId)}
                        loading={deleteLoading}
                    >
                        {tc('delete')}
                    </Button>
                </div>
            </div>

            {/* ────────────────────────────────────────────── */}
            {/* 2. Customer Information Card                    */}
            {/* ────────────────────────────────────────────── */}
            <Card title={t('details.customerInfo')}>
                <div className="customer-details__grid">
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.name')}</span>
                        <span className="customer-details__value">{customer.name}</span>
                    </div>
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.phone')}</span>
                        <span className="customer-details__value customer-details__phone">
                            <a href={`tel:${customer.phone}`}>{customer.phone}</a>
                        </span>
                    </div>
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.nationalId')}</span>
                        <span className="customer-details__value">{customer.nationalId}</span>
                    </div>
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.address')}</span>
                        <span className="customer-details__value">{customer.address}</span>
                    </div>
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.createdAt')}</span>
                        <span className="customer-details__value">{formatDateTime(customer.createdAt)}</span>
                    </div>
                    <div className="customer-details__field">
                        <span className="customer-details__label">{t('details.updatedAt')}</span>
                        <span className="customer-details__value">{formatDateTime(customer.updatedAt)}</span>
                    </div>
                    {customer.createdBy && (
                        <div className="customer-details__field">
                            <span className="customer-details__label">{t('details.createdBy')}</span>
                            <span className="customer-details__value">{customer.createdBy}</span>
                        </div>
                    )}
                </div>
            </Card>

            {/* ────────────────────────────────────────────── */}
            {/* 3. Notes Section                               */}
            {/* ────────────────────────────────────────────── */}
            <div className="customer-details__section">
                <Card title={t('details.notes')}>
                    {customer.notes ? (
                        <p className="customer-details__notes">{customer.notes}</p>
                    ) : (
                        <p className="customer-details__notes customer-details__notes--empty">
                            {t('details.noNotes')}
                        </p>
                    )}
                </Card>
            </div>

            {/* ────────────────────────────────────────────── */}
            {/* 4. Contracts — Stats + Paginated Table         */}
            {/* ────────────────────────────────────────────── */}
            <div className="customer-details__section">
                <Card
                    title={
                        <span className="customer-details__section-title-with-count">
                            {t('details.contracts')}
                            {contractStats.total > 0 && (
                                <span className="customer-details__count-badge">{contractStats.total}</span>
                            )}
                        </span>
                    }
                    footer={
                        <div className="customer-details__card-footer">
                            <Button
                                variant="primary"
                                size="sm"
                                onClick={() =>
                                    navigate(
                                        `${APP_ROUTES.CONTRACTS.CREATE}?customerId=${customer.id}`,
                                    )
                                }
                            >
                                + {t('details.addContract')}
                            </Button>
                        </div>
                    }
                >
                    {contractStats.total > 0 ? (
                        <div className="customer-details__contracts">
                            {/* ── Summary Stats Row ───────── */}
                            <div className="customer-details__contracts-stats">
                                <div className="customer-details__stat">
                                    <span className="customer-details__stat-value">{contractStats.total}</span>
                                    <span className="customer-details__stat-label">{t('details.totalContracts')}</span>
                                </div>
                                <div className="customer-details__stat customer-details__stat--success">
                                    <span className="customer-details__stat-value">{contractStats.active}</span>
                                    <span className="customer-details__stat-label">{t('details.activeContracts')}</span>
                                </div>
                                {contractStats.late > 0 && (
                                    <div className="customer-details__stat customer-details__stat--warning">
                                        <span className="customer-details__stat-value">{contractStats.late}</span>
                                        <span className="customer-details__stat-label">{t('details.lateContracts')}</span>
                                    </div>
                                )}
                                <div className="customer-details__stat customer-details__stat--info">
                                    <span className="customer-details__stat-value">{contractStats.completed}</span>
                                    <span className="customer-details__stat-label">{t('details.completedContracts')}</span>
                                </div>
                                <div className="customer-details__stat">
                                    <span className="customer-details__stat-value">
                                        {formatCurrency(contractStats.totalFinancial)}
                                    </span>
                                    <span className="customer-details__stat-label">{t('details.totalValue')}</span>
                                </div>
                                <div className="customer-details__stat customer-details__stat--warning">
                                    <span className="customer-details__stat-value">
                                        {formatCurrency(contractStats.totalRemaining)}
                                    </span>
                                    <span className="customer-details__stat-label">{t('details.totalRemaining')}</span>
                                </div>
                            </div>

                            {/* ── Contracts Table ─────────── */}
                            <div className="customer-details__contracts-table-wrapper">
                                {contractsLoading ? (
                                    <div className="customer-details__center" style={{ padding: 'var(--spacing-8)' }}>
                                        <LoadingSpinner />
                                    </div>
                                ) : (
                                    <>
                                        <table className="customer-details__table customer-details__contracts-table">
                                            <thead>
                                                <tr>
                                                    <th>{t('details.contractNumber')}</th>
                                                    <th>{t('details.product')}</th>
                                                    <th>{t('details.contractStatus')}</th>
                                                    <th>{t('details.finalPrice')}</th>
                                                    <th>{t('details.monthlyAmount')}</th>
                                                    <th>{t('details.remaining')}</th>
                                                    <th>{t('details.startDate')}</th>
                                                    <th>{t('columns.actions')}</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {contractsToRender.map((contract: any) => (
                                                    <tr
                                                        key={contract.id}
                                                        className="customer-details__contract-row"
                                                        onClick={() =>
                                                            navigate(ROUTE_HELPERS.CONTRACT_VIEW(contract.id))
                                                        }
                                                    >
                                                        <td>
                                                            <span className="customer-details__contract-number">
                                                                {contract.id}
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <div className="customer-details__product-info">
                                                                <span className="customer-details__product-name">
                                                                    {contract.productName}
                                                                </span>
                                                                <span className="customer-details__vendor-name">
                                                                    {contract.vendorName}
                                                                </span>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <span
                                                                className={`customer-details__contract-status customer-details__contract-status--${contract.status.toLowerCase()}`}
                                                            >
                                                                {t(`details.contractStatuses.${contract.status}`)}
                                                            </span>
                                                        </td>
                                                        <td className="customer-details__amount">
                                                            {formatCurrency(contract.finalPrice)}
                                                        </td>
                                                        <td className="customer-details__amount">
                                                            {formatCurrency(contract.monthlyAmount)}
                                                            <span className="customer-details__months">
                                                                ({contract.months} {t('details.months')})
                                                            </span>
                                                        </td>
                                                        <td className="customer-details__amount customer-details__remaining">
                                                            {formatCurrency(contract.remainingAmount)}
                                                        </td>
                                                        <td>{formatDate(contract.startDate)}</td>
                                                        <td>
                                                            <div
                                                                className="customer-details__contract-actions"
                                                                onClick={(e) => e.stopPropagation()}
                                                            >
                                                                <Button
                                                                    size="sm"
                                                                    variant="secondary"
                                                                    onClick={() =>
                                                                        navigate(
                                                                            ROUTE_HELPERS.CONTRACT_VIEW(contract.id),
                                                                        )
                                                                    }
                                                                >
                                                                    {t('details.view')}
                                                                </Button>
                                                                <Button
                                                                    size="sm"
                                                                    variant="secondary"
                                                                    onClick={() =>
                                                                        navigate(
                                                                            ROUTE_HELPERS.CONTRACT_EDIT(contract.id),
                                                                        )
                                                                    }
                                                                >
                                                                    {tc('edit')}
                                                                </Button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>

                                        {/* ── Contracts Pagination ─── */}
                                        {contractsTotalElements > 0 && (
                                            <div className="customer-details__contracts-pagination">
                                                <span className="customer-details__page-info">
                                                    {t('pagination.showing', {
                                                        from: cFrom,
                                                        to: cTo,
                                                        total: contractsTotalElements,
                                                    })}
                                                </span>
                                                <div className="customer-details__page-size">
                                                    <span>{t('pagination.pageSize')}</span>
                                                    <select
                                                        value={cSize}
                                                        onChange={(e) => {
                                                            cSetSize(Number(e.target.value))
                                                            cGoToPage(0)
                                                        }}
                                                    >
                                                        {CONTRACT_PAGE_SIZES.map((s) => (
                                                            <option key={s} value={s}>{s}</option>
                                                        ))}
                                                    </select>
                                                </div>
                                                <div className="customer-details__page-controls">
                                                    <Button
                                                        size="sm"
                                                        variant="secondary"
                                                        onClick={cPrev}
                                                        disabled={cPage === 0}
                                                    >
                                                        {t('pagination.previous')}
                                                    </Button>
                                                    <Button
                                                        size="sm"
                                                        variant="secondary"
                                                        onClick={cNext}
                                                        disabled={cPage >= contractsTotalPages - 1}
                                                    >
                                                        {t('pagination.next')}
                                                    </Button>
                                                </div>
                                            </div>
                                        )}
                                    </>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="customer-details__placeholder">
                            <span className="customer-details__placeholder-icon">📄</span>
                            <span className="customer-details__placeholder-text">
                                {t('details.contractsPlaceholder')}
                            </span>
                            <Button
                                variant="primary"
                                size="sm"
                                onClick={() =>
                                    navigate(
                                        `${APP_ROUTES.CONTRACTS.CREATE}?customerId=${customer.id}`,
                                    )
                                }
                            >
                                + {t('details.createFirstContract')}
                            </Button>
                        </div>
                    )}
                </Card>
            </div>

            {/* ────────────────────────────────────────────── */}
            {/* 5. Linked Accounts                             */}
            {/* ────────────────────────────────────────────── */}
            <div className="customer-details__section">
                <Card
                    title={
                        <span className="customer-details__section-title-with-count">
                            {t('details.linkedAccounts')}
                            {linkedAccounts.length > 0 && (
                                <span className="customer-details__count-badge">{linkedAccounts.length}</span>
                            )}
                        </span>
                    }
                    footer={
                        <div className="customer-details__card-footer">
                            <Button variant="primary" size="sm" onClick={() => setShowLinkModal(true)}>
                                + {t('linkedAccounts.addLink')}
                            </Button>
                        </div>
                    }
                >
                    {linkedAccounts.length > 0 ? (
                        <table className="customer-details__table">
                            <thead>
                                <tr>
                                    <th>{t('details.linkedCustomer')}</th>
                                    <th>{t('details.phone')}</th>
                                    <th>{t('details.relationshipType')}</th>
                                    <th>{t('details.relationshipDescription')}</th>
                                    <th>{t('details.status')}</th>
                                    <th>{t('details.createdAt')}</th>
                                    <th>{t('columns.actions')}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {linkedAccounts.map((link: CustomerAccountLink, idx: number) => (
                                    <tr key={link.id ?? idx}>
                                        <td className="customer-details__linked-name">
                                            {link.linkedCustomerName}
                                        </td>
                                        <td>
                                            <a href={`tel:${link.phone}`}>{link.phone}</a>
                                        </td>
                                        <td>
                                            <span className="customer-details__relationship-badge">
                                                {t(`relationshipType.${link.relationshipType as CustomerRelationshipType}`)}
                                            </span>
                                        </td>
                                        <td>{link.relationshipDescription || '—'}</td>
                                        <td>
                                            <span
                                                className={`customer-details__badge customer-details__badge--${link.active ? 'active' : 'inactive'}`}
                                            >
                                                <span className="customer-details__badge-dot" />
                                                {link.active ? t('details.linkActive') : t('details.linkInactive')}
                                            </span>
                                        </td>
                                        <td>{formatDateTime(link.createdAt)}</td>
                                        <td>
                                            <Button
                                                size="sm"
                                                variant="secondary"
                                                onClick={() => navigate(ROUTE_HELPERS.CUSTOMER_VIEW(link.id))}
                                            >
                                                {t('details.view')}
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <div className="customer-details__placeholder">
                            <span className="customer-details__placeholder-icon">🔗</span>
                            <span className="customer-details__placeholder-text">
                                {t('details.linkedAccountsPlaceholder')}
                            </span>
                            <Button variant="primary" size="sm" onClick={() => setShowLinkModal(true)}>
                                + {t('linkedAccounts.addFirstLink')}
                            </Button>
                        </div>
                    )}
                </Card>
            </div>


            {/* ────────────────────────────────────────────── */}
            {/* Delete Confirmation Modal                       */}
            {/* ────────────────────────────────────────────── */}
            <Modal
                isOpen={isModalOpen}
                onClose={handleCloseModal}
                title={t('deleteConfirm')}
                footer={
                    <>
                        <Button variant="secondary" onClick={handleCloseModal}>
                            {tc('cancel')}
                        </Button>
                        <Button
                            variant="danger"
                            onClick={() => handleDelete(deleteCustomer)}
                            loading={deleteLoading}
                        >
                            {tc('delete')}
                        </Button>
                    </>
                }
            >
                <p>{t('deleteConfirmMessage')}</p>
            </Modal>

            {/* ────────────────────────────────────────────── */}
            {/* Link Account Modal                             */}
            {/* ────────────────────────────────────────────── */}
            <Modal
                isOpen={showLinkModal}
                onClose={() => { setShowLinkModal(false); resetLinkForm() }}
                title={t('linkedAccounts.addLink')}
                footer={
                    <>
                        <Button variant="secondary" onClick={() => { setShowLinkModal(false); resetLinkForm() }}>
                            {tc('cancel')}
                        </Button>
                        <Button
                            variant="primary"
                            onClick={handleLinkAccount}
                            loading={linkLoading}
                            disabled={!selectedCustomer}
                        >
                            {t('linkedAccounts.linkButton')}
                        </Button>
                    </>
                }
            >
                <div className="customer-details__link-form">
                    {/* ── National ID Search ────────────── */}
                    <div className="customer-details__search-field">
                        <Input
                            label={t('linkedAccounts.searchBy')}
                            name="nationalIdSearch"
                            placeholder={t('linkedAccounts.searchPlaceholder')}
                            value={linkForm.nationalIdSearch}
                            onChange={(e) => {
                                setLinkForm((prev) => ({ ...prev, nationalIdSearch: e.target.value }))
                                setSelectedCustomer(null) // clear selection when typing
                            }}
                            required
                        />

                        {/* ── Search Results Dropdown ────── */}
                        {linkForm.nationalIdSearch.length >= 3 && !selectedCustomer && (
                            <div className="customer-details__search-dropdown">
                                {searching ? (
                                    <div className="customer-details__search-loading">
                                        <LoadingSpinner size="sm" />
                                        <span>{t('linkedAccounts.searching')}</span>
                                    </div>
                                ) : searchResults.length > 0 ? (
                                    searchResults.map((c) => (
                                        <button
                                            key={c.id}
                                            type="button"
                                            className="customer-details__search-result"
                                            onClick={() => {
                                                setSelectedCustomer(c)
                                                setLinkForm((prev) => ({ ...prev, nationalIdSearch: c.nationalId }))
                                                setSearchResults([])
                                            }}
                                        >
                                            <span className="customer-details__search-result-name">{c.name}</span>
                                            <span className="customer-details__search-result-details">
                                                {c.nationalId} · {c.phone}
                                            </span>
                                        </button>
                                    ))
                                ) : (
                                    <div className="customer-details__search-empty">
                                        {t('linkedAccounts.noResults')}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* ── Selected Customer Preview ─────── */}
                    {selectedCustomer && (
                        <div className="customer-details__selected-customer">
                            <div className="customer-details__selected-info">
                                <span className="customer-details__selected-name">{selectedCustomer.name}</span>
                                <span className="customer-details__selected-meta">
                                    {t('details.nationalId')}: {selectedCustomer.nationalId}
                                </span>
                                <span className="customer-details__selected-meta">
                                    {t('details.phone')}: {selectedCustomer.phone}
                                </span>
                            </div>
                            <Button
                                size="sm"
                                variant="secondary"
                                onClick={() => {
                                    setSelectedCustomer(null)
                                    setLinkForm((prev) => ({ ...prev, nationalIdSearch: '' }))
                                }}
                            >
                                ✕
                            </Button>
                        </div>
                    )}

                    {/* ── Relationship Type ─────────────── */}
                    <div className="form-field">
                        <label className="form-field__label">{t('details.relationshipType')}</label>
                        <select
                            className="customer-details__select"
                            value={linkForm.relationshipType}
                            onChange={(e) =>
                                setLinkForm((prev) => ({
                                    ...prev,
                                    relationshipType: e.target.value as CustomerRelationshipType,
                                }))
                            }
                        >
                            {Object.values(RelTypes).map((type) => (
                                <option key={type} value={type}>
                                    {t(`relationshipType.${type}`)}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* ── Description ───────────────────── */}
                    <Input
                        label={t('linkedAccounts.description')}
                        name="description"
                        placeholder={t('linkedAccounts.descriptionPlaceholder')}
                        value={linkForm.description}
                        onChange={(e) =>
                            setLinkForm((prev) => ({ ...prev, description: e.target.value }))
                        }

                    />
                </div>
            </Modal>
        </>
    )
}


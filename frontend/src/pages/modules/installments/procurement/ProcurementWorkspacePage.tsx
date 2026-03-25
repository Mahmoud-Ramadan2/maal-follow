import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

import PurchaseListPage from '@pages/modules/installments/purchase/PurchaseListPage'
import VendorListPage from '@pages/modules/installments/vendor/VendorListPage'
import './ProcurementWorkspacePage.css'

/**
 * Unified procurement workspace.
 *
 * This page intentionally composes existing module list pages so both
 * workflows stay in one place while preserving current CRUD logic.
 */
export default function ProcurementWorkspacePage(): ReactNode {
	const { t } = useTranslation('common')

	return (
		<div className="procurement-workspace">
			<header className="procurement-workspace__header">
				<h1 className="procurement-workspace__title">{t('nav.procurement')}</h1>
			</header>

			<section className="procurement-workspace__section" aria-label="Purchases section">
				<PurchaseListPage />
			</section>

			<section className="procurement-workspace__section" aria-label="Vendors section">
				<VendorListPage />
			</section>
		</div>
	)
}



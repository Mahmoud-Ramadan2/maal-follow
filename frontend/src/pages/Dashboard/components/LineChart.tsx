// dependency-free SVG line chart component (no React default import required)

interface LineChartProps {
    labels: string[]
    points: number[]
    height?: number
    stroke?: string
}

// Very small, dependency-free SVG line chart for incremental dashboards
export default function LineChart({ labels, points, height = 140, stroke = 'var(--color-primary)' }: LineChartProps) {
    const max = Math.max(...points, 1)
    const min = Math.min(...points, 0)
    const w = 600
    const h = height
    const pad = 8

    const scaleX = (i: number) => pad + (i / Math.max(labels.length - 1, 1)) * (w - pad * 2)
    const scaleY = (v: number) => h - pad - ((v - min) / Math.max(max - min, 1e-6)) * (h - pad * 2)

    const pathD = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${scaleX(i)} ${scaleY(p)}`).join(' ')

    return (
        <div className="line-chart" role="img" aria-label="line chart">
            <svg viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none" style={{ width: '100%', height: `${h}px` }}>
                <defs>
                    <linearGradient id="lg" x1="0" x2="0" y1="0" y2="1">
                        <stop offset="0%" stopColor={stroke} stopOpacity="0.12" />
                        <stop offset="100%" stopColor={stroke} stopOpacity="0" />
                    </linearGradient>
                </defs>
                {/* area */}
                <path d={`${pathD} L ${scaleX(points.length - 1)} ${h - pad} L ${scaleX(0)} ${h - pad} Z`} fill="url(#lg)" stroke="none" />
                {/* line */}
                <path d={pathD} fill="none" stroke={stroke} strokeWidth={2} strokeLinejoin="round" strokeLinecap="round" />
                {/* small circles */}
                {points.map((p, i) => (
                    <circle key={i} cx={scaleX(i)} cy={scaleY(p)} r={2.5} fill={stroke} />
                ))}
            </svg>
        </div>
    )
}



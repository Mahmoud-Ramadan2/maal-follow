// Small pie chart renderer (no React default import required)

interface Slice { label: string; value: number; color?: string }

interface PieChartProps {
    slices: Slice[]
    size?: number
}

export default function PieChart({ slices, size = 160 }: PieChartProps) {
    const total = Math.max(slices.reduce((s, x) => s + Math.max(0, x.value), 0), 1)
    let acc = 0
    const cx = size / 2
    const cy = size / 2
    const r = size / 2 - 2

    const describeArc = (start: number, end: number) => {
        const startRad = (start - 90) * (Math.PI / 180)
        const endRad = (end - 90) * (Math.PI / 180)
        const x1 = cx + r * Math.cos(startRad)
        const y1 = cy + r * Math.sin(startRad)
        const x2 = cx + r * Math.cos(endRad)
        const y2 = cy + r * Math.sin(endRad)
        const largeArc = end - start <= 180 ? 0 : 1
        return `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`
    }

    return (
        <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} role="img" aria-label="pie chart">
            {slices.map((s, i) => {
                const start = (acc / total) * 360
                acc += Math.max(0, s.value)
                const end = (acc / total) * 360
                return <path key={i} d={describeArc(start, end)} fill={s.color ?? `hsl(${(i * 70) % 360} 70% 50%)`} stroke="#fff" strokeWidth={1} />
            })}
        </svg>
    )
}



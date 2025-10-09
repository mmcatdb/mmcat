import random, { Random } from 'random';



export class RandomHelper {
    private readonly random: Random
    readonly nullDistr: () => number

    constructor(randomGen: Random) {
        this.random = random
        this.nullDistr = this.random.uniform(0, 1)
    }

    string(length: number, chars = 'abcdefghijklmnopqrstuvwxyz'.split('')) {
        const output: string[] = []
        for (let i = 0; i < length; i++) output.push(this.random.choice(chars)!)
        return output.join('')
    }

    // generates a function for repeated sampling
    date(min: Date, max: Date): Date {
        const minN = min.getTime(), maxN = max.getTime()
        return new Date(this.random.int(minN, maxN))
    }

    nullable<T>(nullProbability: number, value: () => T): T | null {
        if (this.nullDistr() < nullProbability) return null
        return value()
    }
}

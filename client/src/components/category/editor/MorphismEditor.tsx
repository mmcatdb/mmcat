import { type KeyboardEvent, useState } from 'react';
import { Button, Input, Radio, RadioGroup } from '@heroui/react';
import { type CategoryEditorForm, type CategoryEditorDispatch, type CategoryEditorState } from './useCategoryEditor';
import { Cardinality, type Min } from '@/types/schema';

type MorphismEditorProps = {
    state: CategoryEditorState;
    dispatch: CategoryEditorDispatch;
};

export function MorphismEditor({ state, dispatch }: MorphismEditorProps) {
    const signature = (state.form as Extract<CategoryEditorForm, { type: 'morphism' }>).signature;
    const morphism = state.evocat.category.morphisms.get(signature)!;

    const [ label, setLabel ] = useState(morphism.metadata.label ?? '');
    const [ minCardinality, setMinCardinality ] = useState<Min>(morphism.schema.min);

    const finalLabel = label.trim() || undefined;

    const isChanged = label !== morphism.metadata.label ||
        minCardinality !== morphism.schema.min;
    const isDisabled = !isChanged;

    function apply() {
        state.evocat.updateMorphism(morphism.schema, {
            min: minCardinality,
            label: finalLabel,
        });

        dispatch({ type: 'morphism' });
    }

    function handleKeyDown(e: KeyboardEvent) {
        if (e.key === 'Enter' && !isDisabled) {
            e.preventDefault();
            apply();
        }
    }

    return (
        <div className='flex flex-col gap-2' onKeyDown={handleKeyDown}>
            <h3 className='text-lg font-semibold'>Update Morphism</h3>

            <p className='my-1 text-default-600'>
                <span className='font-bold'>Signature:</span> {signature.toString()}
            </p>

            <Input
                label='Label'
                value={label}
                onValueChange={setLabel}
                placeholder='no label'
                className='max-w-xs'
            />

            <p className='text-sm'>Minimum Cardinality:</p>

            <RadioGroup
                value={minCardinality}
                onChange={e => setMinCardinality(e.target.value as Min)}
                orientation='horizontal'
            >
                <Radio value={Cardinality.Zero}>0</Radio>
                <Radio value={Cardinality.One}>1</Radio>
            </RadioGroup>

            <div className='mt-1 grid grid-cols-2 gap-2'>
                <Button onPress={() => dispatch({ type: 'form', operation: 'cancel' })}>
                    Cancel
                </Button>

                <Button color='primary' onPress={apply} isDisabled={isDisabled}>
                    Apply
                </Button>
            </div>
        </div>
    );
}

import { type ReactNode, useState } from 'react';
import { type BaseSpinnerButtonProps, CustomLink, SpinnerButton } from '@/components/common/components';
import { routes } from '@/routes/routes';
import { api } from '@/api';
import { SchemaCategoryInfo } from '@/types/schema';
import { Button, Card, CardBody } from '@heroui/react';
import { toast } from 'react-toastify';
import { BookOpenIcon } from '@heroicons/react/24/solid';
import { FaDatabase, FaPlus, FaArrowRight } from 'react-icons/fa';
import { useLoaderData, useNavigate } from 'react-router-dom';
import { PageLayout } from '@/components/RootLayout';
import { CreateSchemaModal, EMPTY_CATEGORY, EXAMPLE_CATEGORIES, useSchemaCategories } from './CategoriesPage';
import { type IconType } from 'react-icons/lib';
import { cn } from '@/components/common/utils';

export function HomePage() {
    const { categories: loadedCategories } = useLoaderData() as HomeLoaderData;
    const [ isModalOpen, setIsModalOpen ] = useState(false);
    const [ showAllCategories, setShowAllCategories ] = useState(false);

    const { categories, fetching, createCategory } = useSchemaCategories(loadedCategories);

    return (
        <PageLayout className='max-w-7xl space-y-16'>
            <HeaderSection />

            <GettingStartedSection
                onOpenModal={() => setIsModalOpen(true)}
                categories={categories}
                fetching={fetching}
            />

            <SchemaCategoriesSection
                categories={categories}
                showAllCategories={showAllCategories}
                setShowAllCategories={setShowAllCategories}
                onOpenModal={() => setIsModalOpen(true)}
                fetching={fetching}
                createCategory={createCategory}
            />

            <CreateSchemaModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                // TODO The fetching animation should be on the button in the modal ...
                onSubmit={label => createCategory(label, EMPTY_CATEGORY, FID_EMPTY_MODAL)}
            />
        </PageLayout>
    );
}

export type HomeLoaderData = {
    categories: SchemaCategoryInfo[];
};

HomePage.loader = async (): Promise<HomeLoaderData> => {
    const response = await api.schemas.getAllCategoryInfos({});
    if (!response.status)
        throw new Error('Failed to load schema categories');

    return {
        categories: response.data.map(SchemaCategoryInfo.fromResponse),
    };
};

const FID_EMPTY_MODAL = 'empty-modal';

function HeaderSection() {
    return (
        <div className='space-y-6 text-center md:text-left'>
            <h1 className='text-5xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                MM-cat
            </h1>
            <p className='text-default-600 text-xl mx-auto'>
                A <span className='font-semibold text-primary-600'>multi-model data modeling framework</span> powered by category theory.
                Model, transform, and explore multi-model data without database limitations.
            </p>
        </div>
    );
}

type GettingStartedSectionProps = {
    onOpenModal: () => void;
    fetching: string | undefined;
    categories?: SchemaCategoryInfo[];
};

function GettingStartedSection({ onOpenModal, fetching, categories }: GettingStartedSectionProps) {
    const navigate = useNavigate();

    return (
        <div className='space-y-8'>
            <div className='text-center'>
                <h2 className='text-3xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                    Get Started in 3 Steps
                </h2>
                <p className='text-default-500 mt-2 max-w-2xl mx-auto'>
                    Quickly set up your multi-model data modeling environment with these simple steps
                </p>
            </div>
            <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                <FeatureCard
                    Icon={FaDatabase}
                    colorClass='bg-primary-100 text-primary-600'
                    title='Connect Data Sources'
                    description='Link your existing databases or files to start modeling your data.'
                    button={props => (
                        <Button
                            {...props}
                            variant='solid'
                            color='primary'
                            onPress={() => navigate(routes.datasources.list.path, { state: { openModal: true } })}
                        >
                            Connect Now
                        </Button>
                    )}
                />

                <FeatureCard
                    Icon={FaPlus}
                    colorClass='bg-secondary-100 text-secondary-600'
                    title='Create Schema Category'
                    description='Start a new project to model your data relationships and structure.'
                    button={props => (
                        <SpinnerButton
                            {...props}
                            variant='solid'
                            color='secondary'
                            onPress={onOpenModal}
                            fetching={fetching}
                            fid={FID_EMPTY_FEATURE}
                        >
                            New Schema
                        </SpinnerButton>
                    )}
                />

                <FeatureCard
                    Icon={BookOpenIcon}
                    colorClass='bg-success-100 text-success-600'
                    title='Define Objects in Editor'
                    description='Open last created schema category and define objects.'
                    button={props => (
                        <Button
                            {...props}
                            variant='solid'
                            color='success'
                            onPress={() => {
                                if (categories && categories.length > 0)
                                    navigate(routes.category.editor.resolve({ categoryId: categories[0].id }));
                                else
                                    toast.error('No schema categories available. Please create one first.');
                            }}
                            isDisabled={!categories || categories.length === 0}
                        >
                            Explore
                        </Button>
                    )}
                />
            </div>
        </div>
    );
}

const FID_EMPTY_FEATURE = 'empty-feature';

type SchemaCategoriesSectionProps = {
    categories: SchemaCategoryInfo[];
    showAllCategories: boolean;
    setShowAllCategories: (state: boolean) => void;
    onOpenModal: () => void;
    createCategory: ReturnType<typeof useSchemaCategories>['createCategory'];
    fetching: string | undefined;
};

function SchemaCategoriesSection({ categories, showAllCategories, setShowAllCategories, onOpenModal, fetching, createCategory }: SchemaCategoriesSectionProps) {
    return (
        <div className='space-y-8'>
            <div className='flex flex-col md:flex-row md:items-end justify-between gap-4'>
                <div className='mt-5'>
                    <h2 className='text-3xl font-bold bg-linear-to-r from-primary-500 to-secondary-500 bg-clip-text text-transparent'>
                        Your Schema Categories
                    </h2>
                    <p className='text-default-500'>
                        Create or explore existing schema categories to model your data
                    </p>
                </div>
                <div className='flex flex-wrap gap-3'>
                    <SpinnerButton
                        onPress={onOpenModal}
                        color='primary'
                        startContent={<FaPlus className='size-4' />}
                        fetching={fetching}
                        fid={FID_EMPTY_MODAL}
                    >
                        New Schema
                    </SpinnerButton>

                    {EXAMPLE_CATEGORIES.map(example => (
                        <SpinnerButton
                            key={example}
                            onPress={() => createCategory(example, example, fidExample(example))}
                            color='secondary'
                            variant='flat'
                            startContent={<FaPlus className='size-4' />}
                            fetching={fetching}
                            fid={fidExample(example)}
                        >
                            Example ({example})
                        </SpinnerButton>
                    ))}
                </div>
            </div>

            {categories.length === 0 ? (
                <div className='text-center border-2 border-dashed border-default-200 p-12 rounded-xl'>
                    <BookOpenIcon className='mx-auto size-12 text-default-300' />
                    <p className='mt-4 text-default-500'>No schema categories yet. Create your first one to get started!</p>
                </div>
            ) : (<>
                <div className='grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4'>
                    {(showAllCategories ? categories : categories.slice(0, 6)).map(category => (
                        <Card
                            key={category.id}
                            isPressable
                            isHoverable
                            className='p-6 hover:border-primary-300 transition-all'
                            shadow='sm'
                        >
                            <CustomLink to={routes.category.index.resolve({ categoryId: category.id })}>
                                <CardBody className='p-0'>
                                    <h3 className='text-lg font-semibold text-primary-600'>{category.label}</h3>
                                    <p className='text-default-500 mt-2'>System version ID: {category.systemVersionId}</p>
                                </CardBody>
                            </CustomLink>
                        </Card>
                    ))}
                </div>

                {categories.length > 6 && (
                    <div className='flex justify-center'>
                        <Button
                            variant='light'
                            onPress={() => setShowAllCategories(!showAllCategories)}
                            className='text-primary-600'
                        >
                            {showAllCategories ? 'Show Less' : `Show All (${categories.length})`}
                        </Button>
                    </div>
                )}
            </>)}
        </div>
    );
}

function fidExample(example: string) {
    return `example-${example}`;
}

type FeatureCardProps = {
    Icon: IconType;
    colorClass: string;
    title: string;
    description: string;
    button?: (props: BaseSpinnerButtonProps) => ReactNode;
};

export function FeatureCard({ Icon, colorClass, title, description, button }: FeatureCardProps) {
    return (
        <Card className='p-6 h-full flex flex-col'>
            <CardBody className='flex flex-col gap-4 h-full p-0'>
                <div className='flex justify-center'>
                    <div className={cn('size-14 rounded-full flex items-center justify-center', colorClass)}>
                        <Icon className='size-7' />
                    </div>
                </div>
                <div className='flex flex-col items-center text-center grow min-h-[120px]'>
                    <h3 className='text-xl font-semibold text-default-800'>
                        <span>{title}</span>
                    </h3>
                    <p className='text-default-600 mt-2'>
                        <span>{description}</span>
                    </p>
                </div>
                <div className='h-[40px] flex items-center justify-center'>
                    {button?.({
                        className: 'w-full max-w-[200px]',
                        endContent: <FaArrowRight className='size-3' />,
                    })}
                </div>
            </CardBody>
        </Card>
    );
}

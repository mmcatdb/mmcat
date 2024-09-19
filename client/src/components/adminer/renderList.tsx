export const renderList = (value: any) => {
    if (typeof value === "object" && !Array.isArray(value)) {
        // If value is an object, create another unordered list for its key-value pairs
        return (
            <ul>
                {Object.entries(value).map(([key, val]) => (
                    <li key={key}>
                        <strong>{key}:</strong> {renderList(val)}
                    </li>
                ))}
            </ul>
        );
    } else if (Array.isArray(value)) {
        // If value is an array, create a list for each item
        return (
            <ul>
                {value.map((item, idx) => (
                    <li key={idx}>{renderList(item)}</li>
                ))}
            </ul>
        );
    } else {
        // For primitive values (string, number, etc.), return them as a string
        return <span>{String(value)}</span>;
    }
};
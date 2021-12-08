import React, { useState } from "react";

import { Button, InputGroup, TextInput } from "@patternfly/react-core";
import { SearchIcon } from "@patternfly/react-icons";

export interface SearchInputProps {
  onSearch: (filterText: string) => void;
}

export const SearchInput: React.FC<SearchInputProps> = ({ onSearch }) => {
  const [filterText, setFilterText] = useState("");

  const callSearch = () => {
    onSearch(filterText);
  };

  const handleOnKeyDown = (event: any) => {
    if (event.key === "Enter") {
      callSearch();
    }
  };

  return (
    <InputGroup>
      <TextInput
        name="filterText"
        type="search"
        aria-label="search input"
        value={filterText}
        onChange={setFilterText}
        onKeyDown={handleOnKeyDown}
        autoComplete="off"
      />
      <Button variant="control" aria-label="search button" onClick={callSearch}>
        <SearchIcon />
      </Button>
    </InputGroup>
  );
};

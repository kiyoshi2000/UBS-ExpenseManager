# TablePagination Component

Componente de paginação reutilizável para tabelas, baseado nos componentes shadcn/ui.

## Características

- ✅ Navegação entre páginas com Previous/Next
- ✅ Seleção direta de páginas
- ✅ Indicadores de ellipsis para muitas páginas
- ✅ Seletor de itens por página (opcional)
- ✅ Contador de resultados
- ✅ Totalmente responsivo
- ✅ Suporte a dark mode

## Instalação

O componente já está instalado no projeto. Ele usa o componente `pagination` da shadcn/ui.

## Uso Básico

```tsx
import { useState, useMemo, useEffect } from "react";
import { TablePagination } from "@/components/Pagination";

function MyListPage() {
  const [data, setData] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  // Filtrar dados
  const filteredData = useMemo(() => {
    return data.filter(item => 
      item.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [data, searchTerm]);

  // Paginar dados
  const paginatedData = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    return filteredData.slice(startIndex, endIndex);
  }, [filteredData, currentPage, itemsPerPage]);

  const totalPages = Math.ceil(filteredData.length / itemsPerPage);

  // Resetar para primeira página quando o filtro mudar
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleItemsPerPageChange = (newItemsPerPage: number) => {
    setItemsPerPage(newItemsPerPage);
    setCurrentPage(1);
  };

  return (
    <div>
      {/* Seu componente de busca/filtro */}
      
      {/* Sua tabela com paginatedData */}
      <DataTable data={paginatedData} />

      {/* Componente de paginação */}
      {filteredData.length > 0 && (
        <TablePagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
          itemsPerPage={itemsPerPage}
          totalItems={filteredData.length}
          onItemsPerPageChange={handleItemsPerPageChange}
        />
      )}
    </div>
  );
}
```

## Props

| Prop | Tipo | Obrigatório | Padrão | Descrição |
|------|------|-------------|--------|-----------|
| `currentPage` | `number` | Sim | - | Página atual (base 1) |
| `totalPages` | `number` | Sim | - | Total de páginas |
| `onPageChange` | `(page: number) => void` | Sim | - | Callback quando a página mudar |
| `itemsPerPage` | `number` | Sim | - | Número de itens por página |
| `totalItems` | `number` | Sim | - | Total de itens (para exibição) |
| `onItemsPerPageChange` | `(itemsPerPage: number) => void` | Não | - | Callback quando itens por página mudar |
| `itemsPerPageOptions` | `number[]` | Não | `[10, 25, 50, 100]` | Opções de itens por página |

## Exemplos

### Sem seletor de itens por página

```tsx
<TablePagination
  currentPage={currentPage}
  totalPages={totalPages}
  onPageChange={setCurrentPage}
  itemsPerPage={10}
  totalItems={data.length}
/>
```

### Com opções customizadas de itens por página

```tsx
<TablePagination
  currentPage={currentPage}
  totalPages={totalPages}
  onPageChange={setCurrentPage}
  itemsPerPage={itemsPerPage}
  totalItems={data.length}
  onItemsPerPageChange={setItemsPerPage}
  itemsPerPageOptions={[5, 10, 20, 50]}
/>
```

## Páginas que usam este componente

- [UsersPage](../../pages/Users/UsersPage.tsx) - Gerenciamento de usuários

## Estilização

O componente usa classes do Tailwind CSS e está totalmente integrado com o design system do projeto, incluindo suporte a dark mode.

## Notas

- O componente automaticamente desabilita os botões Previous/Next quando apropriado
- Os números de página são calculados dinamicamente para mostrar sempre contexto útil
- O seletor de itens por página só aparece se `onItemsPerPageChange` for fornecido
